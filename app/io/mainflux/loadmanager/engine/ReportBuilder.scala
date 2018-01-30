package io.mainflux.loadmanager.engine

import javax.inject.Inject

import akka.actor.{Actor, Props}
import akka.routing.RoundRobinPool
import io.mainflux.loadmanager.engine.LoadRetriever.LoadUpdated
import io.mainflux.loadmanager.engine.ReportSender.Send
import play.api.libs.ws.WSClient

final class ReportBuilder @Inject()(ws: WSClient,
                                    groupRepository: GroupRepository,
                                    subscriberRepository: SubscriberRepository)
    extends Actor {

  import ReportBuilder._
  import context.dispatcher

  private val workers =
    context.actorOf(RoundRobinPool(SenderPoolSize).props(ReportSender.props(ws)), "report-senders")

  def receive: Receive = {
    case BuildReport(updates) =>
      def buildReports(groups: Seq[Group]) = {
        val loadsByGroups = groups.map { group =>
          val loads = updates.collect {
            case update if group.microgrids.contains(update.grid) => update.load
          }

          group.info.id.getOrElse(0) -> loads.sum
        }

        loadsByGroups.toMap
      }

      val allReports     = groupRepository.retrieveAll.map(buildReports)
      val allSubscribers = subscriberRepository.retrieveAll

      for {
        reports     <- allReports
        subscribers <- allSubscribers
      } yield {
        subscribers.foreach { subscriber =>
          subscriber.groups.foreach { group =>
            workers ! Send(subscriber.info.callback, reports(group))
          }
        }
      }
  }
}

object ReportBuilder {
  // TODO: load this value from configuration
  private val SenderPoolSize = 10

  private[engine] final case class BuildReport(loads: Seq[LoadUpdated])

  def props(ws: WSClient,
            groupRepository: GroupRepository,
            subscriberRepository: SubscriberRepository): Props =
    Props(new ReportBuilder(ws, groupRepository, subscriberRepository))
}
