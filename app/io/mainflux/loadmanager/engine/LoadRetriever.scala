package io.mainflux.loadmanager.engine

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.DateTime

final class LoadRetriever(microgrid: Microgrid) extends Actor with ActorLogging {
  import LoadRetriever._

  def receive: Receive = {
    case UpdateLoad => log.debug("Retrieving load for {}", microgrid.id)
  }
}

object LoadRetriever {
  private[engine] case object UpdateLoad
  private[engine] final case class LoadUpdated(grid: Long, load: Double, time: DateTime)

  def props(microgrid: Microgrid): Props = Props(new LoadRetriever(microgrid))
}
