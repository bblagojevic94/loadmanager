package io.mainflux.loadmanager.engine

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.DateTime
import akka.testkit.{EventFilter, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import io.mainflux.loadmanager.UnitSpec
import io.mainflux.loadmanager.engine.LoadRetriever.LoadUpdated
import io.mainflux.loadmanager.engine.ReportGenerator.{AskingForUpdates, InitialTick, RetrievedLoad, Tick}
import io.mainflux.loadmanager.engine.ReportSender.Report
import org.mockito.Mockito._

import scala.concurrent.Future
import scala.concurrent.duration._

final class ReportGeneratorSpec
    extends TestKit(ActorSystem("test-system", ReportGeneratorSpec.Config))
    with UnitSpec {

  "Report generator" should "initially ask for load updates" in {
    val probe      = TestProbe()
    val repository = mockedRepository()
    val generator  = system.actorOf(ReportGenerator.props(probe.ref, repository))

    EventFilter.debug(message = AskingForUpdates, occurrences = 1).intercept {
      initialize(generator)
    }

    verify(repository, times(1)).retrieveAll
  }

  it should "periodically ask for load updates" in {
    val probe      = TestProbe()
    val repository = mockedRepository()
    val generator  = system.actorOf(ReportGenerator.props(probe.ref, repository))

    initialize(generator)

    EventFilter.debug(message = AskingForUpdates, occurrences = 1).intercept {
      generator ! Tick
    }

    verify(repository, times(2)).retrieveAll
  }

  it should "store update information locally" in {
    val update     = LoadUpdated(1, 2.0, DateTime.now)
    val probe      = TestProbe()
    val repository = mockedRepository()
    val generator  = system.actorOf(ReportGenerator.props(probe.ref, repository))

    initialize(generator)

    EventFilter.debug(start = RetrievedLoad, occurrences = 1).intercept {
      generator ! update
    }
  }

  it should "report current loads periodically" in {
    val probe      = TestProbe()
    val repository = mockedRepository()
    val generator  = system.actorOf(ReportGenerator.props(probe.ref, repository))

    initialize(generator)
    generator ! Tick

    within(5.seconds) {
      probe.expectMsg(Report(Map.empty))
    }
  }

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  private def initialize(generator: ActorRef): Unit = generator ! InitialTick

  private def mockedRepository(): MicrogridRepository = {
    val repository = mock[MicrogridRepository]
    when(repository.retrieveAll).thenReturn(Future.successful(Seq.empty[Microgrid]))
    repository
  }
}

object ReportGeneratorSpec {
  private val Config =
    ConfigFactory.parseString("""
        |akka.loggers = ["akka.testkit.TestEventListener"]
        |akka.loglevel = "DEBUG"
      """.stripMargin)
}
