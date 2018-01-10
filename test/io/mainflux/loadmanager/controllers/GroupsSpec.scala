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

final class GroupsSpec extends ControllerSpec {

  val eh = app.injector.instanceOf[HttpErrorHandler]

  "Group controller" should "respond with 201 given valid group creation request" in new Fixture {
    when(repository.save(any[Group])).thenReturn(Future.successful(group))

    val reqBody = Json.parse(
      """
        |{
        |  "data": {
        |    "type": "groups",
        |    "attributes": {
        |      "name": "name-1"
        |    },
        |    "relationships": {
        |      "microgrids": {
        |        "data": [
        |          { "id": 1, "type": "microgrids" },
        |          { "id": 2, "type": "microgrids" }
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
    val response = contentAsJson(result).as[GroupResponse]

    response.data.id should be(group.info.id)
    response.data.`type` should be(GroupType)
    response.data.links.self should be(s"/$GroupType/$id")

    response.data.attributes.name should be(group.info.name)
    response.data.relationships.microgrids.links.self should be(
      s"/$GroupType/$id/relationships/$MicrogridType"
    )

    (response.data.relationships.microgrids.data.map(_.id) should contain)
      .allElementsOf(group.microgrids)
  }

  it should "respond with 200 given valid group retrieval request" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(Some(group)))

    val result = underTest.retrieveOne(id)(FakeRequest())

    status(result) should be(OK)
    val response = contentAsJson(result).as[GroupResponse]

    response.data.id should be(group.info.id)
    response.data.`type` should be(GroupType)
    response.data.links.self should be(s"/$GroupType/$id")

    response.data.attributes.name should be(group.info.name)
    response.data.relationships.microgrids.links.self should be(
      s"/$GroupType/$id/relationships/$MicrogridType"
    )

    (response.data.relationships.microgrids.data.map(_.id) should contain)
      .allElementsOf(group.microgrids)
  }

  it should "respond with 404 given non-existing group identifier in group retrieval request" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(None))

    an[EntityNotFound] should be thrownBy await(
      underTest.retrieveOne(id).apply(FakeRequest())
    )
  }

  it should "respond with 200 given valid groups retrieval request" in new Fixture {
    val groups = (0 until 3).map(_.toLong).map(makeGroup)
    when(repository.retrieveAll).thenReturn(Future.successful(groups))

    val result = underTest.retrieveAll()(FakeRequest())

    status(result) should be(OK)
    val response = contentAsJson(result).as[GroupCollectionResponse]

    response.data.size should be(groups.size)
    all(response.data.map(_.`type`)) should be(GroupType)
    (response.data.flatMap(_.relationships.microgrids.data.map(_.id)) should contain)
      .allElementsOf(group.microgrids)
  }

  it should "respond with 204 given valid group removal request" in new Fixture {
    when(repository.remove(id)).thenReturn(Future.successful(1))

    val result = underTest.remove(id)(FakeRequest())
    status(result) should be(NO_CONTENT)
  }

  it should "respond with 404 given non-existing group identifier in group removal request" in new Fixture {
    when(repository.remove(id)).thenReturn(Future.successful(0))

    an[EntityNotFound] should be thrownBy await(underTest.remove(id).apply(FakeRequest()))
  }

  it should "respond with 200 given valid related microgrids retrieval request" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(Some(group)))
    val result = underTest.retrieveMicrogrids(id)(FakeRequest())

    status(result) should be(OK)
    val response = contentAsJson(result).as[MicrogridIdentifiers]

    (response.data.map(_.id) should contain).allElementsOf(group.microgrids)
    all(response.data.map(_.`type`)) should be(MicrogridType)
  }

  it should "respond with 404 given non-existing group identifier in realated microgrids retrieval request" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(None))

    an[EntityNotFound] should be thrownBy await(
      underTest.retrieveMicrogrids(id).apply(FakeRequest())
    )
  }

  it should "respond with 204 when adding valid microgrids to group" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(Some(group)))
    when(repository.addMicrogrids(id, Set(10, 11))).thenReturn(Future.successful(Some(2)))

    val reqBody = Json.parse("""
                               |{
                               |  "data": [
                               |    { "id": 10, "type": "microgrids" },
                               |    { "id": 11, "type": "microgrids" }
                               |  ]
                               |}
                             """.stripMargin)
    val request = FakeRequest().withBody[JsValue](reqBody)
    val result  = underTest.addMicrogrids(id)(request)

    status(result) should be(NO_CONTENT)
  }

  it should "respond with 404 given non-existing group identifier when adding microgrids to group" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(None))

    val reqBody = Json.parse("""
                               |{
                               |  "data": [
                               |    { "id": 10, "type": "microgrids" },
                               |    { "id": 11, "type": "microgrids" }
                               |  ]
                               |}
                             """.stripMargin)
    val request = FakeRequest().withBody[JsValue](reqBody)

    an[EntityNotFound] should be thrownBy await(
      underTest.addMicrogrids(id)(request)
    )
  }

  it should "respond with 204 given microgrid that is already member of group when adding microgrids to group" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(Some(group)))
    when(repository.addMicrogrids(id, Set(10))).thenReturn(Future.successful(Some(1)))

    val reqBody = Json.parse("""
                               |{
                               |  "data": [
                               |    { "id": 1, "type": "microgrids" },
                               |    { "id": 10, "type": "microgrids" }
                               |  ]
                               |}
                             """.stripMargin)
    val request = FakeRequest().withBody[JsValue](reqBody)
    await(underTest.addMicrogrids(id)(request))

    verify(repository, times(1)).addMicrogrids(id, Set(10))
  }

  it should "respond with 204 when removing existing microgrids from group" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(Some(group)))
    when(repository.removeMicrogrids(id, Set(1))).thenReturn(Future.successful(1))

    val reqBody = Json.parse("""
                               |{
                               |  "data": [
                               |    { "id": 1, "type": "microgrids" }
                               |  ]
                               |}
                             """.stripMargin)
    val request = FakeRequest().withBody[JsValue](reqBody)
    val result  = underTest.removeMicrogrids(id)(request)

    status(result) should be(NO_CONTENT)
  }

  it should "respond with 404 when trying to remove microgrid from non-existent group" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(None))

    val reqBody = Json.parse("""
                               |{
                               |  "data": [
                               |    { "id": 0, "type": "microgrids" },
                               |    { "id": 1, "type": "microgrids" }
                               |  ]
                               |}""".stripMargin)
    val request = FakeRequest().withBody[JsValue](reqBody)

    an[EntityNotFound] should be thrownBy await(
      underTest.removeMicrogrids(id)(request)
    )
  }

  it should "respond with 204 when trying to remove non-member microgrid from group" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(Some(group)))
    when(repository.removeMicrogrids(id, Set(1))).thenReturn(Future.successful(1))

    val reqBody = Json.parse("""
                               |{
                               |  "data": [
                               |    { "id": 1, "type": "microgrids" },
                               |    { "id": 10, "type": "microgrids" }
                               |  ]
                               |}
                             """.stripMargin)
    val request = FakeRequest().withBody[JsValue](reqBody)
    await(underTest.removeMicrogrids(id)(request))

    verify(repository, times(1)).removeMicrogrids(id, Set(1))
  }

  it should "prevent bulk update of related microgrids" in new Fixture {
    val result = underTest.updateMicrogrids(id)(FakeRequest())
    status(result) should be(FORBIDDEN)
  }

  trait Fixture {
    val cc         = stubControllerComponents()
    val repository = mock[GroupRepository]

    val underTest = new Groups(repository, cc, eh)

    def makeGroup(id: Long) = {
      val info = GroupInfo(Some(id), s"name-$id", LocalDateTime.now())
      Group(info, Set(1, 2))
    }

    val id    = 1L
    val group = makeGroup(id)
  }
}
