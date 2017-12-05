package io.mainflux.loadmanager.engine

import akka.actor.{ActorLogging, ActorRef, Props, Stash, Timers}
import akka.pattern.pipe
import akka.routing.FromConfig
import com.google.inject.Inject
import io.mainflux.loadmanager.osgp.Worker
import io.mainflux.loadmanager.osgp.Worker.CalculateLoad

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Subscriptions {

  case object TickKey
  case object Tick

  sealed trait Event
  final case class AddMicrogrid(microgrid: Microgrid, groupId: Long)           extends Event
  final case class RemoveGroup(groupId: Long)                                  extends Event
  final case class RemoveMicrogrid(microgridId: Long, groupId: Long)           extends Event
  final case class SubscribeOnGroup(groupId: Long, subscription: Subscription) extends Event
  final case class RemoveSubscriber(subscriberId: Long)                        extends Event
  final case class UnsubscribeFromGroup(subscriberId: Long, groupId: Long)     extends Event

  final case class State(microgrids: Map[Long, Map[Long, MicrogridLoad]] = Map(),
                         subscriptions: Map[Long, Seq[Subscription]] = Map())

  final case class InitializationDone(state: State)
  final case class AggregateLoad(groupId: Long, microgrid: Grid, load: Double)
  final case class NotifySubscribers(groupId: Long, subscribers: Seq[Subscription], aggregateLoad: Double)

  final case class MicrogridLoad(microgrid: Grid, value: Option[Double] = None)

  private val PoolingPeriod: FiniteDuration = 1.minute
}

class Subscriptions @Inject()(groupRepository: GroupRepository,
                              subscriptionRepository: SubscriptionRepository)(
    implicit val ec: ExecutionContext
) extends Timers
    with ActorLogging
    with Stash {

  import Subscriptions._

  val osgpWorkers: ActorRef = context.actorOf(FromConfig.props(Props[Worker]), "osgp")

  override def preStart: Unit = {
    val initValues = for {
      microgrids <- groupRepository.retrieveAll.map {
        _.map(group => group.id.get -> group.grids.map(grid => grid.id.get -> MicrogridLoad(grid)).toMap).toMap
      }
      subscriptions <- subscriptionRepository.retrieveAll.map {
        _.flatMap(s => s.groupIds.map(_ -> s)).groupBy(_._1).mapValues(_.map(_._2))
      }
    } yield (microgrids, subscriptions)

    initValues
      .map {
        case (microgrids, subscriptions) =>
          InitializationDone(State(microgrids, subscriptions))
      }
      .pipeTo(context.self)
  }

  override def receive: Receive = uninitialized

  def uninitialized: Receive = {
    case InitializationDone(state) =>
      context.become(initialized(state))
      unstashAll()
    case state: State =>
      timers.startPeriodicTimer(TickKey, Tick, PoolingPeriod)
      context.self ! InitializationDone(state)
    case _ => stash()
  }

  def initialized(state: State): Receive = {
    case Tick =>
      state.microgrids.foreach {
        case (groupId, gridLoads) =>
          calculateLoad(groupId, gridLoads.values.map(_.microgrid).toSeq)
      }
    case event: Event =>
      val updatedState = updateState(state, event)
      context.become(initialized(updatedState))
    case AggregateLoad(groupId, microgrid, load) =>
      val entry = microgrid.id.get -> MicrogridLoad(microgrid, Option(load))

      state.microgrids.get(groupId).foreach { groupState =>
        val updatedMicrogrids = state.microgrids + (groupId -> (state.microgrids(groupId) + entry))

        if (isCalculatingFinished(updatedMicrogrids, groupId)) {
          val aggregateLoad: Double =
            updatedMicrogrids.get(groupId).map(_.values.flatMap(_.value).sum).getOrElse(0)

          val groupResetState = groupState.mapValues(mgl => MicrogridLoad(mgl.microgrid))
          val resetMicrogrids = state.microgrids + (groupId -> groupResetState)

          val subscribers = state.subscriptions.getOrElse(groupId, Seq())
          self ! NotifySubscribers(groupId, subscribers, aggregateLoad)
          context.become(initialized(state.copy(microgrids = resetMicrogrids)))
        } else {
          context.become(initialized(state.copy(microgrids = updatedMicrogrids)))
        }
      }
    case NotifySubscribers(groupId, subscribers, aggregateLoad) =>
      log.debug("Aggregate load for group with id: {} is {}", groupId, aggregateLoad)
  }

  private def updateState(state: State, event: Event): State =
    event match {
      case AddMicrogrid(microgrid, groupId) =>
        val entry     = microgrid.id.get -> MicrogridLoad(microgrid)
        val gridLoads = state.microgrids.get(groupId).fold(Map(entry))(_ + entry)
        state.copy(microgrids = state.microgrids + (groupId -> gridLoads))
      case RemoveGroup(groupId) =>
        state.copy(microgrids = state.microgrids - groupId, subscriptions = state.subscriptions - groupId)
      case RemoveMicrogrid(microgridId, groupId) =>
        val microgrids = state.microgrids
          .get(groupId)
          .fold(state.microgrids) { gridLoads =>
            state.microgrids + (groupId -> (gridLoads - microgridId))
          }
        state.copy(microgrids = microgrids)
      case SubscribeOnGroup(groupId, subscription) =>
        val entry = state.subscriptions.get(groupId).fold(Seq(subscription))(_ :+ subscription)
        state.copy(subscriptions = state.subscriptions + (groupId -> entry))
      case RemoveSubscriber(subscriberId) =>
        state.copy(subscriptions = state.subscriptions - subscriberId)
      case UnsubscribeFromGroup(subscriberId, groupId) =>
        val subscriptions = state.subscriptions
          .get(groupId)
          .fold(state.subscriptions) { subs =>
            state.subscriptions + (groupId -> subs.filterNot(_.id.get == subscriberId))
          }
        state.copy(subscriptions = subscriptions)
    }

  private def calculateLoad(groupId: Long, grids: Seq[Grid]): Unit =
    grids.foreach {
      case mg: Microgrid =>
        mg.platform match {
          case Platform.OSGP     => osgpWorkers ! CalculateLoad(groupId, mg)
          case Platform.MAINFLUX => log.debug("Mainflux platform is not supported yet")
        }
    }

  private def isCalculatingFinished(state: Map[Long, Map[Long, MicrogridLoad]], groupId: Long) =
    state.get(groupId).exists(_.values.toList.forall(_.value.isDefined))
}
