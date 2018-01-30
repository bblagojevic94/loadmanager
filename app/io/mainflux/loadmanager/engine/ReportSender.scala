package io.mainflux.loadmanager.engine

import java.time.LocalDate

import akka.actor.{Actor, ActorLogging, Props}
import play.api.libs.ws.WSClient

import scala.util.{Failure, Success}

final class ReportSender(ws: WSClient) extends Actor with ActorLogging {
  import ReportSender._
  import context.dispatcher

  def receive: Receive = {
    case Send(url, load) =>
      log.debug("Sending {} to {}...", load, url)

      val body =
        s"""
           |{
           |  "data": {
           |    "type": "reports",
           |    "attributes": {
           |      "aggregatedLoad": $load
           |    }
           |  },
           |  "meta": {
           |    "createdAt": "${LocalDate.now()}"
           |  }
           |}
         """.stripMargin

      ws.url(url)
        .withHttpHeaders("Content-Type" -> "application/vnd.api+json")
        .post(body)
        .onComplete {
          case Success(_) =>
            log.info("Successfully sent {} to {}", load, url)
          case Failure(e) =>
            log.warning("Failed to send {} to {} due to {}", load, url, e.getMessage)
        }
  }
}

object ReportSender {
  private[engine] final case class Send(url: String, load: Double)

  def props(ws: WSClient): Props = Props(new ReportSender(ws))
}
