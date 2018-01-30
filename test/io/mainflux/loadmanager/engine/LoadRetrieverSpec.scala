package io.mainflux.loadmanager.engine

import java.time.{Clock, Instant, LocalDateTime, ZoneId}

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import io.mainflux.loadmanager.UnitSpec
import io.mainflux.loadmanager.engine.LoadRetriever.{LoadUpdated, UpdateLoad}
import org.mockito.Matchers.any
import org.mockito.Mockito.when

import scala.concurrent.Future
import scala.concurrent.duration._

final class LoadRetrieverSpec
    extends TestKit(ActorSystem("test-system", LoadRetrieverSpec.Config))
    with UnitSpec {

  "Load retriever" should "calculate load and notify its parent" in {
    val clock    = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    val now      = LocalDateTime.now(clock)
    val mg       = microgrid()
    val provider = mockedProvider()

    val reportGenerator = TestProbe()
    val loadRetriever   = reportGenerator.childActorOf(LoadRetriever.props(mg, provider, clock))

    loadRetriever ! UpdateLoad

    reportGenerator.expectMsg(5.seconds, LoadUpdated(mg.id.getOrElse(0), 10.0, now))
  }

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  private def microgrid(): Microgrid =
    Microgrid(Some(1), s"test-url", Platform.OSGP, s"org", LocalDateTime.now())

  private def mockedProvider() = {
    val client = new PlatformClient {
      override def loadOf(microgrid: Microgrid): Future[Double] = Future.successful(10.0)
    }

    val provider = mock[ClientProvider]
    when(provider.clientFor(any[Platform])).thenReturn(client)

    provider
  }
}

object LoadRetrieverSpec {
  private val Config = {
    val cfg =
      """
        |akka.loggers = ["akka.testkit.TestEventListener"]
        |akka.loglevel = "DEBUG"
      """.stripMargin

    ConfigFactory.parseString(cfg)
  }
}
