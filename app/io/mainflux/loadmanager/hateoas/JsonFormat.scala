package io.mainflux.loadmanager.hateoas

import play.api.libs.json.{Json, OFormat}

object JsonFormat {

  implicit val links: OFormat[Links] = Json.format[Links]

  implicit val meta: OFormat[Meta]                   = Json.format[Meta]
  implicit val error: OFormat[Error]                 = Json.format[Error]
  implicit val errorResponse: OFormat[ErrorResponse] = Json.format[ErrorResponse]

  implicit val microGridIdentifier: OFormat[MicroGridIdentifier] = Json.format[MicroGridIdentifier]
  implicit val microGridIdentifierCollection: OFormat[MicroGridIdentifierCollection] =
    Json.format[MicroGridIdentifierCollection]

  implicit val groupRelationshipsRequest: OFormat[GroupRelationshipsRequest] =
    Json.format[GroupRelationshipsRequest]
  implicit val groupAttributes: OFormat[GroupAttributes] = Json.format[GroupAttributes]
  implicit val groupData: OFormat[GroupData]             = Json.format[GroupData]
  implicit val groupRequest: OFormat[GroupRequest]       = Json.format[GroupRequest]
  implicit val microgridsRelationships: OFormat[MicrogridsRelationships] =
    Json.format[MicrogridsRelationships]
  implicit val groupRelationshipsResponse: OFormat[GroupRelationshipsResponse] =
    Json.format[GroupRelationshipsResponse]
  implicit val groupResponseData: OFormat[GroupResponseData] = Json.format[GroupResponseData]
  implicit val groupResponse: OFormat[GroupResponse]         = Json.format[GroupResponse]

}
