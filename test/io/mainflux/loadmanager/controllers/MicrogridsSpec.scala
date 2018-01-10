package io.mainflux.loadmanager.controllers

import java.time.LocalDateTime

import io.mainflux.loadmanager.engine.{EntityNotFound, Microgrid, MicrogridRepository, PlatformType}
import io.mainflux.loadmanager.hateoas._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.http.HttpErrorHandler
import play.api.libs.json.{JsString, JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class MicrogridsSpec extends ControllerSpec {

  val eh = app.injector.instanceOf[HttpErrorHandler]

  "Microgrids controller " should "respond with 200 given valid microgrid retrieval request" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(Some(microgrid)))

    val result = underTest.retrieveOne(id)(FakeRequest())

    status(result) should be(OK)
    val response = contentAsJson(result).as[MicrogridResponse]

    response.data.id should be(microgrid.id)
    response.data.`type` should be(MicrogridType)
    response.data.links.self should be(s"/$MicrogridType/$id")

    response.data.attributes should have(
      'url (microgrid.url),
      'platformType (microgrid.platform.toString),
      'organisationId (microgrid.organisationId)
    )
  }

  it should "respond with 404 given non-existing microgrid identifier in microgrid retrieval request" in new Fixture {
    when(repository.retrieveOne(id)).thenReturn(Future.successful(None))

    an[EntityNotFound] should be thrownBy await(
      underTest.retrieveOne(id).apply(FakeRequest())
    )
  }

  it should "respond with 201 given valid microgrid creation request" in new Fixture {
    when(repository.save(any[Microgrid])).thenReturn(Future.successful(microgrid))

    val reqBody = Json.parse(
      """
        |{
        |  "data": {
        |    "type": "microgrids",
        |    "attributes": {
        |      "url": "test-url-1",
        |      "platformType": "OSGP",
        |      "organisationId": "org-1"
        |    }
        |  }
        |}
      """.stripMargin
    )
    val request = FakeRequest().withBody[JsValue](reqBody)

    val result = underTest.create().apply(request)

    status(result) should be(CREATED)
    val response = contentAsJson(result).as[MicrogridResponse]

    response.data.id should be(microgrid.id)
    response.data.`type` should be(MicrogridType)
    response.data.links.self should be(s"/$MicrogridType/$id")

    response.data.attributes should have(
      'url (microgrid.url),
      'platformType (microgrid.platform.toString),
      'organisationId (microgrid.organisationId)
    )
  }

  it should "respond with 200 given valid microgrids retrieval request" in new Fixture {
    val microgrids = (0 until 3).map(_.toLong).map(makeMicrogrid)
    when(repository.retrieveAll).thenReturn(Future.successful(microgrids))

    val result = underTest.retrieveAll()(FakeRequest())

    status(result) should be(OK)
    val json     = contentAsJson(result)
    val response = json.as[MicrogridCollectionResponse]

    response.data.size should be(microgrids.size)
    all(json \\ "type") should be(JsString(MicrogridType))
  }

  it should "respond with 204 given valid microgrid removal request" in new Fixture {
    when(repository.remove(id)).thenReturn(Future.successful(1))

    val result = underTest.remove(id)(FakeRequest())
    status(result) should be(NO_CONTENT)
  }

  it should "respond with 404 given non-existing microgrid identifier in microgrid removal request" in new Fixture {
    when(repository.remove(id)).thenReturn(Future.successful(0))

    an[EntityNotFound] should be thrownBy await(underTest.remove(id).apply(FakeRequest()))
  }

  trait Fixture {
    val cc         = stubControllerComponents()
    val repository = mock[MicrogridRepository]

    val underTest = new Microgrids(repository, cc, eh)

    def makeMicrogrid(id: Long) =
      Microgrid(Some(id), s"test-url-$id", PlatformType.OSGP, s"org-$id", LocalDateTime.now())

    val id        = 1L
    val microgrid = makeMicrogrid(id)
  }
}
