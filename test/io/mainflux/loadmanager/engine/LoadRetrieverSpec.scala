package io.mainflux.loadmanager.engine

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.http.scaladsl.model.DateTime
import akka.testkit.{TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import io.mainflux.loadmanager.UnitSpec
import io.mainflux.loadmanager.engine.LoadRetriever.{LoadUpdated, UpdateLoad}
import org.joda.time.DateTimeUtils
import org.mockito.Matchers.any
import org.mockito.Mockito.when

import scala.concurrent.Future
import scala.concurrent.duration._

final class LoadRetrieverSpec
    extends TestKit(ActorSystem("test-system", LoadRetrieverSpec.Config))
    with UnitSpec {

  "Load retriever" should "calculate load and notify it's parent" in {
    DateTimeUtils.setCurrentMillisFixed(DateTime.now.clicks)

    val mg       = microgrid()
    val provider = mockedProvider()

    val reportGenerator = TestProbe()
    val loadRetriever   = reportGenerator.childActorOf(LoadRetriever.props(provider, mg))

    loadRetriever ! UpdateLoad

    reportGenerator.expectMsg(5.seconds, LoadUpdated(mg.id.getOrElse(0), 10D, DateTime.now))

    DateTimeUtils.setCurrentMillisSystem()
  }

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  private def microgrid(): Microgrid =
    Microgrid(Some(1), s"test-url", Platform.OSGP, s"org", LocalDateTime.now())

  private def mockedProvider() = {
    val provider = mock[ClientProvider]
    when(provider.clientFor(any[Platform])).thenReturn(new PlatformClient {
      override def loadOf(microgrid: Microgrid): Future[Double] = Future.successful(10D)
    })
    provider
  }
}

object LoadRetrieverSpec {
  private val Config =
    ConfigFactory.parseString("""
                                |akka.loggers = ["akka.testkit.TestEventListener"]
                                |akka.loglevel = "DEBUG"
                              """.stripMargin)
}
