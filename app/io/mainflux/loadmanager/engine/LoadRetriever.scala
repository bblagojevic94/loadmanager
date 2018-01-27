package io.mainflux.loadmanager.engine

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.DateTime

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

final class LoadRetriever(provider: ClientProvider, microgrid: Microgrid)
    extends Actor
    with ActorLogging {
  import LoadRetriever._

  implicit val ec: ExecutionContext = context.dispatcher

  private val platformClient: PlatformClient = provider.clientFor(microgrid.platform)

  def receive: Receive = {
    case UpdateLoad =>
      log.debug("Retrieving load for {}", microgrid.id.getOrElse(0))
      platformClient.loadOf(microgrid).onComplete {
        case Success(load) =>
          context.parent ! LoadUpdated(microgrid.id.getOrElse(0), load, DateTime.now)
        case Failure(_) =>
          log.error("Unable to retrieve load for {}", microgrid.id.getOrElse(0))
      }
  }
}

object LoadRetriever {
  private[engine] case object UpdateLoad
  private[engine] final case class LoadUpdated(grid: Long, load: Double, time: DateTime)

  def props(provider: ClientProvider, microgrid: Microgrid): Props =
    Props(new LoadRetriever(provider, microgrid))
}
