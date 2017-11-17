package io.mainflux.loadmanager.hateoas

import io.mainflux.loadmanager.engine.{Microgrid, Platform}
import org.scalatest.{MustMatchers, WordSpecLike}

class MicrogridSpec extends WordSpecLike with MustMatchers {

  "MicrogridHateoas" when {
    "parsing microgrid request" should {
      "create valid Microgrid" in {
        val attributes =
          MicrogridAttributes(url = "test-url", platformType = "OSGP", organisationId = "test-org")
        val request = MicrogridRequest(MicrogridRequestData(MicrogridType, attributes))

        val microgrid = request.data.toDomain
        microgrid must have(
          'url ("test-url"),
          'platform (Platform.OSGP),
          'organisationId ("test-org")
        )
      }
    }

    "creating microgrid response" should {
      "create valid response" in new Fixture {
        val response: MicrogridResponse = MicrogridResponse.fromDomain(microgrid)

        response.data must have(
          'id (1),
          'type (MicrogridType)
        )

        response.data.attributes must have(
          'url ("test-url"),
          'platformType (Platform.MAINFLUX.toString.toUpperCase),
          'organisationId ("test-org")
        )

        response.data.links.self must be(s"/$MicrogridType/1")
      }
    }

    "creating microgrid collection" should {
      "create valid response" in new Fixture {
        val response: MicrogridCollectionResponse = MicrogridCollectionResponse.fromDomain(microgrids)
        (response.data.map(_.id) must contain).allElementsOf(microgrids.map(_.id.get))
      }
    }

    "creating collection of microgrid identifiers" should {
      "create valid response" in new Fixture {
        val ids: Seq[Long]                 = microgrids.map(_.id.get)
        val response: MicrogridIdentifiers = MicrogridIdentifiers.fromDomain(ids)
        (response.data.map(_.id) must contain).allElementsOf(ids)
        all(response.data.map(_.`type`)) must be(MicrogridType)
      }
    }
  }

  trait Fixture {
    val microgrid =
      Microgrid(id = Option(1), url = "test-url", platform = Platform.MAINFLUX, organisationId = "test-org")

    val microgrids = Seq(
      Microgrid(Option(1), "test-url-1", Platform.OSGP, "org-1"),
      Microgrid(Option(2), "test-url-2", Platform.MAINFLUX, "org-2"),
      Microgrid(Option(3), "test-url-3", Platform.OSGP, "org-3")
    )
  }
}
