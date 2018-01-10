package io.mainflux.loadmanager.controllers

import java.time.LocalDateTime

import io.mainflux.loadmanager.engine._
import io.mainflux.loadmanager.hateoas._
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.http.HttpErrorHandler
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class SubscribersSpec extends ControllerSpec {

  val eh = app.injector.instanceOf[HttpErrorHandler]

  "Subscriber controller" should "respond with 201 given valid subscriber creation request" in new Fixture {
    when(repository.save(any[Subscriber])).thenReturn(Future.successful(subscriber))

    val reqBody = Json.parse(
      """
        |{
        |  "data": {
        |    "type": "subscribers",
        |    "attributes": {
        |      "callback": "test-callback-1"
        |    },
        |    "relationships": {
        |      "groups": {
        |        "data": [
        |          { "id": 1, "type": "groups"},
        |          { "id": 2, "type": "groups"}
        |        ]
        |      }
        |    }
        |  }
        |}
      """.stripMargin
    )
    val request = FakeRequest().withBody[JsValue](reqBody)
    val result  = underTest.create().apply(request)

    status(result) should be(CREATED)
    val response = contentAsJson(result).as[SubscriberResponse]

    response.data.id should be(subscriber.info.id)
    response.data.`type` should be(SubscriberType)
    response.data.links.self should be(s"/$SubscriberType/$id")

    response.data.attributes.callback should be(subscriber.info.callback)
    response.data.relationships.groups.links.self should be(
      s"/$SubscriberType/$id/relationships/$GroupType"
    )

    (response.data.relationships.groups.data.map(_.id) should contain)
      .allElementsOf(subscriber.groups)
  }

  it should "respond with 200 given valid subscriber retrieval request" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(Some(subscriber)))

    val result = underTest.retrieveOne(id)(FakeRequest())

    status(result) should be(OK)
    val response = contentAsJson(result).as[SubscriberResponse]

    response.data.id should be(subscriber.info.id)
    response.data.`type` should be(SubscriberType)
    response.data.links.self should be(s"/$SubscriberType/$id")

    response.data.attributes.callback should be(subscriber.info.callback)
    response.data.relationships.groups.links.self should be(
      s"/$SubscriberType/$id/relationships/$GroupType"
    )

    (response.data.relationships.groups.data.map(_.id) should contain)
      .allElementsOf(subscriber.groups)
  }

  it should "respond with 404 given non-existing subscriber identifier in subscriber retrieval request" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(None))

    an[EntityNotFound] should be thrownBy await(
      underTest.retrieveOne(id).apply(FakeRequest())
    )
  }

  it should "respond with 200 given valid subscribers retrieval request" in new Fixture {
    val subscribers = (0 until 3).map(_.toLong).map(makeSubscriber)
    when(repository.retrieveAll).thenReturn(Future.successful(subscribers))

    val result = underTest.retrieveAll()(FakeRequest())

    status(result) should be(OK)
    val response = contentAsJson(result).as[SubscriberCollectionResponse]

    response.data.size should be(subscribers.size)
    all(response.data.map(_.`type`)) should be(SubscriberType)
    (response.data.flatMap(_.relationships.groups.data.map(_.id)) should contain)
      .allElementsOf(subscriber.groups)
  }

  it should "respond with 204 given valid subscriber removal request" in new Fixture {
    when(repository.remove(id)).thenReturn(Future.successful(1))

    val result = underTest.remove(id)(FakeRequest())
    status(result) should be(NO_CONTENT)
  }

  it should "respond with 404 given non-existing subscriber identifier in subscriber removal request" in new Fixture {
    when(repository.remove(id)).thenReturn(Future.successful(0))

    an[EntityNotFound] should be thrownBy await(underTest.remove(id).apply(FakeRequest()))
  }

  it should "respond with 200 given valid related groups retrieval request" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(Some(subscriber)))
    val result = underTest.retrieveGroups(id)(FakeRequest())

    status(result) should be(OK)
    val response = contentAsJson(result).as[GroupIdentifiers]

    (response.data.map(_.id) should contain).allElementsOf(subscriber.groups)
    all(response.data.map(_.`type`)) should be(GroupType)
  }

  it should "respond with 404 given non-existing subscriber identifier in related groups retrieval request" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(None))

    an[EntityNotFound] should be thrownBy await(
      underTest.retrieveGroups(id).apply(FakeRequest())
    )
  }

  it should "respond with 204 when adding valid groups to subscriber" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(Some(subscriber)))
    when(repository.subscribe(id, Set(10, 11))).thenReturn(Future.successful(Some(2)))

    val reqBody = Json.parse("""
                               |{
                               |  "data": [
                               |    { "id": 10, "type": "groups" },
                               |    { "id": 11, "type": "groups" }
                               |  ]
                               |}
                             """.stripMargin)
    val request = FakeRequest().withBody[JsValue](reqBody)
    val result  = underTest.subscribe(id)(request)

    status(result) should be(NO_CONTENT)
  }

  it should "respond with 404 given non-existing subscriber identifier when adding groups to subscriber" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(None))

    val reqBody = Json.parse("""
                               |{
                               |  "data": [
                               |    { "id": 10, "type": "groups" },
                               |    { "id": 11, "type": "groups" }
                               |  ]
                               |}
                             """.stripMargin)
    val request = FakeRequest().withBody[JsValue](reqBody)

    an[EntityNotFound] should be thrownBy await(
      underTest.subscribe(id)(request)
    )
  }

  it should "respond with 204 given group that subscriber is already subscribed on when adding groups to subscriber" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(Some(subscriber)))
    when(repository.subscribe(id, Set(10))).thenReturn(Future.successful(Some(1)))

    val reqBody = Json.parse("""
                               |{
                               |  "data": [
                               |    { "id": 1, "type": "groups" },
                               |    { "id": 10, "type": "groups" }
                               |  ]
                               |}
                             """.stripMargin)
    val request = FakeRequest().withBody[JsValue](reqBody)
    await(underTest.subscribe(id)(request))

    verify(repository, times(1)).subscribe(id, Set(10))
  }

  it should "respond with 204 when removing existing groups from subscriber" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(Some(subscriber)))
    when(repository.unsubscribe(id, Set(1))).thenReturn(Future.successful(1))

    val reqBody = Json.parse("""
                               |{
                               |  "data": [
                               |    { "id": 1, "type": "groups" }
                               |  ]
                               |}
                             """.stripMargin)
    val request = FakeRequest().withBody[JsValue](reqBody)
    val result  = underTest.unsubscribe(id)(request)

    status(result) should be(NO_CONTENT)
  }

  it should "respond with 404 when trying to remove group from non-existent subscriber" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(None))

    val reqBody = Json.parse("""
                               |{
                               |  "data": [
                               |    { "id": 1, "type": "groups" },
                               |    { "id": 2, "type": "groups" }
                               |  ]
                               |}""".stripMargin)
    val request = FakeRequest().withBody[JsValue](reqBody)

    an[EntityNotFound] should be thrownBy await(
      underTest.unsubscribe(id)(request)
    )
  }

  it should "respond with 204 when trying to remove non-member group from subscriber" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(Some(subscriber)))
    when(repository.unsubscribe(id, Set(1))).thenReturn(Future.successful(1))

    val reqBody = Json.parse("""
                               |{
                               |  "data": [
                               |    { "id": 1, "type": "groups" },
                               |    { "id": 10, "type": "groups" }
                               |  ]
                               |}
                             """.stripMargin)
    val request = FakeRequest().withBody[JsValue](reqBody)
    await(underTest.unsubscribe(id)(request))

    verify(repository, times(1)).unsubscribe(id, Set(1))
  }

  it should "prevent bulk update of related groups" in new Fixture {
    val result = underTest.updateGroups(id)(FakeRequest())
    status(result) should be(FORBIDDEN)
  }

  trait Fixture {
    val cc         = stubControllerComponents()
    val repository = mock[SubscriberRepository]

    val underTest = new Subscribers(repository, cc, eh)

    def makeSubscriber(id: Long) = {
      val info = SubscriberInfo(Some(id), s"test-callback-$id", LocalDateTime.now())
      Subscriber(info, Set(1, 2))
    }

    val id         = 1L
    val subscriber = makeSubscriber(id)
  }
}
