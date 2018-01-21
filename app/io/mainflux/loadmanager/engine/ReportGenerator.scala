package io.mainflux.loadmanager.engine

import javax.inject.{Inject, Named}

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}
import com.google.common.annotations.VisibleForTesting
import io.mainflux.loadmanager.engine.LoadRetriever.{LoadUpdated, UpdateLoad}
import io.mainflux.loadmanager.engine.ReportSender.Report

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

final class ReportGenerator @Inject()(@Named("report-sender") reportSender: ActorRef,
                                      microgridRepository: MicrogridRepository)
    extends Actor
    with ActorLogging
    with Timers {

  import ReportGenerator._

  implicit val ec: ExecutionContext = context.dispatcher

  timers.startSingleTimer(TickKey, InitialTick, 1.minute)

  def receive: Receive = notInitialized

  private def notInitialized: Receive = {
    case InitialTick =>
      askForLoadUpdates()
      context.become(initialized(Map.empty))
      timers.startPeriodicTimer(TickKey, Tick, 5.minute)
  }

  private def initialized(loads: Map[Long, LoadUpdated]): Receive = {
    case Tick =>
      reportCurrent(loads)
      askForLoadUpdates()
    case update @ LoadUpdated(grid, load, time) =>
      log.debug("{} {} for grid {} at {}", RetrievedLoad, load, grid, time)

      val updatedLoads = loads.updated(grid, update)

      context.become(initialized(updatedLoads))
  }

  private def askForLoadUpdates(): Unit = {
    def getOrCreateRetriever(grid: Microgrid) = {
      val name = s"${grid.organisationId}-${grid.id.getOrElse(0)}"

      context.child(name) match {
        case Some(actor) => actor
        case None        => context.actorOf(LoadRetriever.props(grid), name)
      }
    }

    log.debug(AskingForUpdates)

    microgridRepository.retrieveAll.foreach {
      _.foreach(getOrCreateRetriever(_) ! UpdateLoad)
    }
  }

  private def reportCurrent(loads: Map[Long, LoadUpdated]): Unit = reportSender ! Report(loads)
}

object ReportGenerator {
  private case object TickKey
  private[engine] case object InitialTick
  private[engine] case object Tick

  @VisibleForTesting private[engine] val AskingForUpdates = "Asking for load updates..."
  @VisibleForTesting private[engine] val RetrievedLoad    = "Retrieved load"

  def props(reportSender: ActorRef, microgridRepository: MicrogridRepository): Props =
    Props(new ReportGenerator(reportSender, microgridRepository))
}
