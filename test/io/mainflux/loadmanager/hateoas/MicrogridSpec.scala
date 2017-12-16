package io.mainflux.loadmanager.hateoas

import java.time.LocalDateTime

import io.mainflux.loadmanager.UnitSpec
import io.mainflux.loadmanager.engine.{Microgrid, PlatformType}

final class MicrogridSpec extends UnitSpec {
  "Microgrid entity" must "be created from valid request" in {
    val attributes = MicrogridAttributes("test-url", "OSGP", "test-org")
    val request    = MicrogridRequest(MicrogridRequestData(MicrogridType, attributes))

    request.data.toDomain must have(
      'url (attributes.url),
      'platform (PlatformType.OSGP),
      'organisationId (attributes.organisationId)
    )
  }

  it must "not be created from request with invalid platform type" in {
    val attributes = MicrogridAttributes("url", "invalid-platform-type", "org")

    an[IllegalArgumentException] must be thrownBy {
      MicrogridRequestData(MicrogridType, attributes).toDomain
    }
  }

  it must "not be created from request with invalid data type" in {
    val attributes = MicrogridAttributes("test-url", "OSGP", "test-org")

    an[IllegalArgumentException] must be thrownBy {
      MicrogridRequestData("invalid-type", attributes)
    }
  }

  "Microgrid response representation" must "be created from given model" in {
    val mg       = microgrid(1)
    val response = MicrogridResponse.fromDomain(mg)

    response.data must have(
      'id (mg.id),
      'type (MicrogridType)
    )

    response.data.attributes must have(
      'url (mg.url),
      'platformType (mg.platform.toString.toUpperCase),
      'organisationId (mg.organisationId)
    )

    response.data.links.self must be(s"/$MicrogridType/1")
  }

  "Microgrid collection response" must "be created from sequence of microgrids" in {
    val microgrids = (0 until 3).map(_.toLong).map(microgrid)
    val response   = MicrogridCollectionResponse.fromDomain(microgrids)
    (response.data.map(_.id) must contain).allElementsOf(microgrids.map(_.id))
  }

  "Microgrid identifiers" must "be extracted from sequence of microgrids" in {
    val ids      = (0 until 3).map(_.toLong).toSet
    val response = MicrogridIdentifiers.fromDomain(ids)

    (response.data.map(_.id) must contain).allElementsOf(ids)
    all(response.data.map(_.`type`)) must be(MicrogridType)
  }

  "Sequence of microgrid identifiers" must "be extracted from valid request" in {
    val mgIds = MicrogridIdentifiers(
      (0 until 3).map(id => MicrogridIdentifier(MicrogridType, id.toLong)).toSet
    )
    val ids = mgIds.toDomain

    (mgIds.data.map(_.id) must contain).allElementsOf(ids)
  }

  private def microgrid(id: Long) =
    Microgrid(Some(id), s"test-url-$id", PlatformType.OSGP, s"org-$id", LocalDateTime.now())
}
