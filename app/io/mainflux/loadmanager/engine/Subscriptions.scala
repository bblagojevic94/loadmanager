package io.mainflux.loadmanager.engine

import akka.actor.{ActorLogging, ActorRef, Props, Stash, Timers}
import akka.routing.FromConfig
import com.google.inject.Inject
import io.mainflux.loadmanager.osgp.Worker
import io.mainflux.loadmanager.osgp.Worker.CalculateLoad
import play.api.Configuration

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Subscriptions {

  case object TickKey
  case object Tick

  sealed trait Event
  final case class AddMicrogrid(microgrid: Microgrid, groupId: Long) extends Event
  final case class RemoveGroup(groupId: Long)                        extends Event

  final case class InitializationDone(state: Map[Long, Map[Long, MicrogridLoad]])
  final case class AggregateLoad(groupId: Long, microgrid: Grid, load: Double)
  final case class NotifySubscribers(groupId: Long, aggregateLoad: Double)

  final case class MicrogridLoad(microgrid: Grid, value: Option[Double] = None)

  private val PoolingPeriod: FiniteDuration = 1.minute
}

class Subscriptions @Inject()(groupRepository: GroupRepository, conf: Configuration)(
    implicit val ec: ExecutionContext
) extends Timers
    with ActorLogging
    with Stash {

  import Subscriptions._

  val osgpWorkers: ActorRef = context.actorOf(FromConfig.props(Props[Worker]), "osgp")

  override def preStart: Unit =
    groupRepository.retrieveAll.map { groups =>
      val state = groups
        .map(group => group.id.get -> group.grids.map(grid => grid.id.get -> MicrogridLoad(grid)).toMap)
        .toMap

      timers.startPeriodicTimer(TickKey, Tick, PoolingPeriod)
      context.self ! InitializationDone(state)
    }

  override def receive: Receive = uninitialized

  def uninitialized: Receive = {
    case InitializationDone(state) =>
      unstashAll
      context.become(initialized(state))
    case _ => stash()
  }

  def initialized(state: Map[Long, Map[Long, MicrogridLoad]]): Receive = {
    case Tick =>
      state.foreach {
        case (groupId, gridLoads) =>
          calculateLoad(groupId, gridLoads.values.map(_.microgrid).toSeq)
      }
    case event: Event =>
      val updatedState = updateState(state, event)
      context.become(initialized(updatedState))
    case AggregateLoad(groupId, microgrid, load) =>
      val entry = microgrid.id.get -> MicrogridLoad(microgrid, Option(load))

      state.get(groupId).foreach { groupState =>
        val updatedState = state + (groupId -> (state(groupId) + entry))

        if (isCalculatingFinished(updatedState, groupId)) {
          val aggregateLoad: Double =
            updatedState.get(groupId).map(_.values.flatMap(_.value).sum).getOrElse(0)

          val groupResetState = groupState.mapValues(mgl => MicrogridLoad(mgl.microgrid))
          val resetState      = state + (groupId -> groupResetState)

          self ! NotifySubscribers(groupId, aggregateLoad)
          context.become(initialized(resetState))
        } else {
          context.become(initialized(updatedState))
        }
      }
    case NotifySubscribers(groupId, aggregateLoad) =>
      log.debug("Aggregate load for group with id: {} is {}", groupId, aggregateLoad)
  }

  private def updateState(state: Map[Long, Map[Long, MicrogridLoad]],
                          event: Event): Map[Long, Map[Long, MicrogridLoad]] =
    event match {
      case AddMicrogrid(microgrid, groupId) =>
        val entry: (Long, MicrogridLoad) = microgrid.id.get -> MicrogridLoad(microgrid)
        state + (groupId -> state.get(groupId).fold(Map(entry))(_ + entry))
      case RemoveGroup(groupId) =>
        state - groupId
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
