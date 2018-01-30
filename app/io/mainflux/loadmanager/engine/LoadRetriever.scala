package io.mainflux.loadmanager.engine

import java.time.{Clock, LocalDateTime}

import akka.actor.{Actor, ActorLogging, Props}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

final class LoadRetriever(microgrid: Microgrid, provider: ClientProvider, clock: Clock)
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
          context.parent ! LoadUpdated(microgrid.id.getOrElse(0), load, LocalDateTime.now(clock))
        case Failure(_) =>
          log.error("Unable to retrieve load for {}", microgrid.id.getOrElse(0))
      }
  }
}

object LoadRetriever {
  private[engine] case object UpdateLoad
  private[engine] final case class LoadUpdated(grid: Long, load: Double, time: LocalDateTime)

  def props(microgrid: Microgrid,
            provider: ClientProvider,
            clock: Clock = Clock.systemUTC()): Props =
    Props(new LoadRetriever(microgrid, provider, clock))
}
