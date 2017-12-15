package io.mainflux.loadmanager.hateoas

import java.time.LocalDateTime

import io.mainflux.loadmanager.engine.{Microgrid, PlatformType}
import org.apache.commons.lang3.StringUtils.EMPTY

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

    MicrogridResponseData(
      id = microgrid.id,
      `type` = MicrogridType,
      attributes = attributes,
      links = Links(s"/$MicrogridType/${microgrid.id.map(_.toString).getOrElse(EMPTY)}")
    )
  }
}

final case class MicrogridRequest(data: MicrogridRequestData)

final case class MicrogridRequestData(`type`: String, attributes: MicrogridAttributes) {
  def toDomain: Microgrid =
    Microgrid(
      id = None,
      url = attributes.url,
      platform = PlatformType.valueOf(attributes.platformType.toUpperCase),
      organisationId = attributes.organisationId,
      createdAt = LocalDateTime.now()
    )
}

final case class MicrogridAttributes(url: String, platformType: String, organisationId: String)

final case class MicrogridResponse(data: MicrogridResponseData)

object MicrogridResponse {
  def fromDomain(microgrid: Microgrid): MicrogridResponse =
    MicrogridResponse(MicrogridResponseData.fromDomain(microgrid))
}

final case class MicrogridIdentifier(`type`: String, id: Long)

final case class MicrogridIdentifiers(data: Set[MicrogridIdentifier]) {
  def toDomain: Set[Long] = data.map(_.id)
}

object MicrogridIdentifiers {
  def fromDomain(microgrids: Set[Long]): MicrogridIdentifiers =
    MicrogridIdentifiers(microgrids.map { microgridId =>
      MicrogridIdentifier(MicrogridType, microgridId)
    })
}
