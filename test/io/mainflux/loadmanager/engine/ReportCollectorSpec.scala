package io.mainflux.loadmanager.engine

import java.time.LocalDateTime

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{EventFilter, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import io.mainflux.loadmanager.UnitSpec
import io.mainflux.loadmanager.engine.LoadRetriever.LoadUpdated
import io.mainflux.loadmanager.engine.ReportCollector.{InitialTick, Tick}
import io.mainflux.loadmanager.engine.ReportBuilder.BuildReport
import org.mockito.Matchers.any
import org.mockito.Mockito._

import scala.concurrent.Future
import scala.concurrent.duration._

final class ReportCollectorSpec
    extends TestKit(ActorSystem("test-system", ReportCollectorSpec.Config))
    with UnitSpec {

  "Report collector" should "initially ask for load updates" in {
    val probe      = TestProbe()
    val repository = mockedRepository()
    val provider   = mockedProvider()
    val generator  = system.actorOf(ReportCollector.props(probe.ref, provider, repository))

    initialize(generator)

    eventually(timeout(5.seconds)) {
      verify(repository, times(1)).retrieveAll
    }
  }

  it should "periodically ask for load updates" in {
    val probe      = TestProbe()
    val repository = mockedRepository()
    val provider   = mockedProvider()
    val generator  = system.actorOf(ReportCollector.props(probe.ref, provider, repository))

    initialize(generator)
    generator ! Tick

    eventually(timeout(5.seconds)) {
      verify(repository, times(2)).retrieveAll
    }
  }

  it should "store update information locally" in {
    val update     = LoadUpdated(1, 2.0, LocalDateTime.now)
    val probe      = TestProbe()
    val repository = mockedRepository()
    val provider   = mockedProvider()
    val generator  = system.actorOf(ReportCollector.props(probe.ref, provider, repository))

    initialize(generator)

    EventFilter.debug(start = "Retrieved load", occurrences = 1).intercept {
      generator ! update
    }
  }

  it should "trigger report sending periodically" in {
    val probe      = TestProbe()
    val repository = mockedRepository()
    val provider   = mockedProvider()
    val generator  = system.actorOf(ReportCollector.props(probe.ref, provider, repository))

    initialize(generator)
    generator ! Tick

    probe.expectMsg(5.seconds, BuildReport(Seq()))
  }

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  private def initialize(generator: ActorRef): Unit = generator ! InitialTick

  private def mockedRepository(): MicrogridRepository = {
    val repository = mock[MicrogridRepository]
    when(repository.retrieveAll).thenReturn(Future.successful(Seq.empty[Microgrid]))
    repository
  }

  private def mockedProvider() = {
    val provider = mock[ClientProvider]
    when(provider.clientFor(any[Platform])).thenReturn(new PlatformClient {
      override def loadOf(microgrid: Microgrid): Future[Double] = Future.successful(10D)
    })
    provider
  }
}

object ReportCollectorSpec {
  private val Config = {
    val cfg =
      """
        |akka.loggers = ["akka.testkit.TestEventListener"]
        |akka.loglevel = "DEBUG"
      """.stripMargin

    ConfigFactory.parseString(cfg)
  }
}
