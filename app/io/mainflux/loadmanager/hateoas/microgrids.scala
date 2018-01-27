package io.mainflux.loadmanager.hateoas

import java.time.LocalDateTime

import io.mainflux.loadmanager.controllers.routes
import io.mainflux.loadmanager.engine.{Microgrid, Platform}

final case class MicrogridCollectionResponse(data: Seq[MicrogridResponseData])

object MicrogridCollectionResponse {
  def fromDomain(microgrids: Seq[Microgrid]) =
    MicrogridCollectionResponse(microgrids.map(MicrogridResponseData.fromDomain))
}

final case class MicrogridResponseData(id: Option[Long],
                                       `type`: String,
                                       attributes: MicrogridAttributes,
                                       links: Links)

object MicrogridResponseData {
  def fromDomain(microgrid: Microgrid): MicrogridResponseData = {
    val attributes =
      MicrogridAttributes(microgrid.url, microgrid.platform.toString, microgrid.organisationId)

    MicrogridResponseData(microgrid.id,
                          MicrogridType,
                          attributes,
                          Links(microgrid.id.fold("")(id => routes.Microgrids.retrieveOne(id).url)))
  }
}

final case class MicrogridRequest(data: MicrogridRequestData)

final case class MicrogridRequestData(`type`: String, attributes: MicrogridAttributes) {
  require(`type`.equalsIgnoreCase(MicrogridType), s"Invalid type ${`type`} provided.")

  def toDomain: Microgrid =
    Microgrid(None,
              attributes.url,
              Platform.valueOf(attributes.platformType.toUpperCase),
              attributes.organisationId,
              LocalDateTime.now())
}

final case class MicrogridAttributes(url: String, platformType: String, organisationId: String)

final case class MicrogridResponse(data: MicrogridResponseData)

object MicrogridResponse {
  def fromDomain(microgrid: Microgrid): MicrogridResponse =
    MicrogridResponse(MicrogridResponseData.fromDomain(microgrid))
}

final case class MicrogridIdentifier(`type`: String, id: Long) {
  require(`type`.equalsIgnoreCase(MicrogridType), s"Invalid type ${`type`} provided.")
}

final case class MicrogridIdentifiers(data: Set[MicrogridIdentifier]) {
  def toDomain: Set[Long] = data.map(_.id)
}

object MicrogridIdentifiers {
  def fromDomain(microgrids: Set[Long]): MicrogridIdentifiers =
    MicrogridIdentifiers(microgrids.map { id =>
      MicrogridIdentifier(MicrogridType, id)
    })
}
