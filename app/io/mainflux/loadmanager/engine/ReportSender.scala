package io.mainflux.loadmanager.engine

import akka.actor.{Actor, ActorLogging}
import io.mainflux.loadmanager.engine.LoadRetriever.LoadUpdated

final class ReportSender extends Actor with ActorLogging {
  import ReportSender._

  def receive: Receive = {
    case Report(loads) => log.debug("Microgrid loads: {}", loads)
  }
}

object ReportSender {
  final case class Report(loads: Map[Long, LoadUpdated])
}
