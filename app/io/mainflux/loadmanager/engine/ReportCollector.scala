package io.mainflux.loadmanager.engine

import javax.inject.{Inject, Named}

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}
import akka.pattern.pipe
import io.mainflux.loadmanager.engine.LoadRetriever.{LoadUpdated, UpdateLoad}
import io.mainflux.loadmanager.engine.ReportBuilder.BuildReport

import scala.concurrent.duration._

final class ReportCollector @Inject()(@Named("report-builder") reportBuilder: ActorRef,
                                      clientProvider: ClientProvider,
                                      microgridRepository: MicrogridRepository)
    extends Actor
    with ActorLogging
    with Timers {

  import ReportCollector._
  import context.dispatcher

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
      log.debug("Retrieved load {} for grid {} at {}", load, grid, time)
      val updatedLoads = loads.updated(grid, update)
      context.become(initialized(updatedLoads))
    case grids: Seq[Microgrid] => grids.foreach(retrieveLoad)
  }

  private def askForLoadUpdates(): Unit = microgridRepository.retrieveAll.pipeTo(self)

  private def retrieveLoad(grid: Microgrid): Unit = {
    val name = s"${grid.organisationId}-${grid.id.getOrElse(0)}"

    val retriever = context.child(name) match {
      case Some(actor) => actor
      case None        => context.actorOf(LoadRetriever.props(grid, clientProvider), name)
    }

    retriever ! UpdateLoad
  }

  private def reportCurrent(loads: Map[Long, LoadUpdated]): Unit =
    reportBuilder ! BuildReport(loads.values.toSeq)
}

object ReportCollector {
  private case object TickKey
  private[engine] case object InitialTick
  private[engine] case object Tick

  def props(reportSender: ActorRef,
            clientProvider: ClientProvider,
            microgridRepository: MicrogridRepository): Props =
    Props(new ReportCollector(reportSender, clientProvider, microgridRepository))
}
