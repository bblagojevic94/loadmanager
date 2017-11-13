package io.mainflux.loadmanager.hateoas

import play.api.libs.json.{Json, OFormat}

object JsonFormat {

  implicit val links: OFormat[Links]            = Json.format[Links]
  implicit val meta: OFormat[Meta]              = Json.format[Meta]
  implicit val error: OFormat[Error]            = Json.format[Error]
  implicit val errorRes: OFormat[ErrorResponse] = Json.format[ErrorResponse]

  implicit val microgridId: OFormat[MicrogridIdentifier]      = Json.format[MicrogridIdentifier]
  implicit val microgridIdColl: OFormat[MicrogridIdentifiers] = Json.format[MicrogridIdentifiers]

  implicit val groupRelReq: OFormat[GroupRelationshipsRequest]  = Json.format[GroupRelationshipsRequest]
  implicit val groupAttrs: OFormat[GroupAttributes]             = Json.format[GroupAttributes]
  implicit val groupData: OFormat[GroupData]                    = Json.format[GroupData]
  implicit val groupReq: OFormat[GroupRequest]                  = Json.format[GroupRequest]
  implicit val microgridsRel: OFormat[MicrogridsRelationships]  = Json.format[MicrogridsRelationships]
  implicit val groupRelRes: OFormat[GroupRelationshipsResponse] = Json.format[GroupRelationshipsResponse]
  implicit val groupResData: OFormat[GroupResponseData]         = Json.format[GroupResponseData]
  implicit val groupRes: OFormat[GroupResponse]                 = Json.format[GroupResponse]

}
