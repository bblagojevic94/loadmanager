package io.mainflux.loadmanager.engine

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.testkit.{EventFilter, TestKit}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import com.typesafe.config.ConfigFactory
import io.mainflux.loadmanager.UnitSpec
import io.mainflux.loadmanager.engine.LoadRetriever.LoadUpdated
import io.mainflux.loadmanager.engine.ReportBuilder.BuildReport
import play.api.test.WsTestClient
import org.mockito.Mockito._

import scala.concurrent.Future

final class ReportBuilderSpec
    extends TestKit(ActorSystem("test-system", ReportBuilderSpec.Config))
    with UnitSpec {

  "Report builder" should "prepare group reports and send them to the subscribers" in new Fixtures {
    stubFor(post(url).willReturn(ok()))

    WsTestClient.withClient { client =>
      val builder =
        system.actorOf(ReportBuilder.props(client, groupRepository, subscriberRepository))

      EventFilter.info(start = "Successfully sent", occurrences = 1).intercept {
        builder ! BuildReport(Seq(event))
      }
    }
  }

  it should "log all report delivery failures" in new Fixtures {
    stubFor(post(url).willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)))

    WsTestClient.withClient { client =>
      val builder =
        system.actorOf(ReportBuilder.props(client, groupRepository, subscriberRepository))

      EventFilter.warning(start = "Failed to send", occurrences = 1).intercept {
        builder ! BuildReport(Seq(event))
      }
    }
  }

  trait Fixtures {
    val now        = LocalDateTime.now()
    val host       = "http://localhost:8080"
    val url        = "/cb"
    val id         = 1L
    val event      = LoadUpdated(id, 2.0, now)
    val group      = Group(GroupInfo(Some(id), "test-group", now), Set(id))
    val subscriber = Subscriber(SubscriberInfo(None, s"$host$url", now), Set(id))

    val groupRepository      = mock[GroupRepository]
    val subscriberRepository = mock[SubscriberRepository]

    when(groupRepository.retrieveAll).thenReturn(Future.successful(Seq(group)))
    when(subscriberRepository.retrieveAll).thenReturn(Future.successful(Seq(subscriber)))
  }

  private val server = new WireMockServer()

  override protected def beforeAll(): Unit = server.start()

  override protected def afterAll(): Unit = {
    server.stop()
    TestKit.shutdownActorSystem(system)
  }
}

object ReportBuilderSpec {
  private val Config = {
    val cfg = """akka.loggers = ["akka.testkit.TestEventListener"]"""
    ConfigFactory.parseString(cfg)
  }
}
