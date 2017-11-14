package io.mainflux.loadmanager.hateoas

import io.mainflux.loadmanager.engine.{Microgrid, Platform}

final case class MicrogridRequest(data: MicrogridRequestData)

final case class MicrogridRequestData(`type`: String, attributes: MicrogridAttributes) {
  def toDomain: Microgrid =
    Microgrid(url = attributes.url,
              platform = Platform.valueOf(attributes.platformType.toUpperCase),
              organisationId = attributes.organisationId)
}

final case class MicrogridAttributes(url: String, platformType: String, organisationId: String)

final case class MicrogridResponse(data: MicrogridResponseData)

object MicrogridResponse {
  def fromDomain(microgrid: Microgrid): MicrogridResponse =
    MicrogridResponse(MicrogridResponseData.fromDomain(microgrid))
}

final case class MicrogridResponseData(id: Long,
                                       `type`: String,
                                       attributes: MicrogridAttributes,
                                       links: Links)

object MicrogridResponseData {
  def fromDomain(microgrid: Microgrid): MicrogridResponseData = {
    val attributes = MicrogridAttributes(microgrid.url, microgrid.platform.toString, microgrid.organisationId)

    MicrogridResponseData(
      id = microgrid.id.get,
      `type` = MicrogridType,
      attributes = attributes,
      links = Links(s"/$MicrogridType/${microgrid.id.get}")
    )
  }
}

final case class MicrogridCollectionResponse(data: Seq[MicrogridResponseData])

object MicrogridCollectionResponse {
  def fromDomain(microgrids: Seq[Microgrid]) =
    MicrogridCollectionResponse(microgrids.map(MicrogridResponseData.fromDomain))
}

final case class MicrogridIdentifier(`type`: String, id: Long)

final case class MicrogridIdentifiers(data: Seq[MicrogridIdentifier])
