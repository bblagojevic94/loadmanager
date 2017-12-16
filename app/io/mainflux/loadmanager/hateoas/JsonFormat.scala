package io.mainflux.loadmanager.hateoas

import play.api.libs.json.{Json, OFormat}

trait JsonFormat {
  implicit val meta: OFormat[Meta]              = Json.format[Meta]
  implicit val error: OFormat[Error]            = Json.format[Error]
  implicit val errorRes: OFormat[ErrorResponse] = Json.format[ErrorResponse]
  implicit val links: OFormat[Links]            = Json.format[Links]

  implicit val microgridId: OFormat[MicrogridIdentifier]        = Json.format[MicrogridIdentifier]
  implicit val microgridIdColl: OFormat[MicrogridIdentifiers]   = Json.format[MicrogridIdentifiers]
  implicit val microgridAttrs: OFormat[MicrogridAttributes]     = Json.format[MicrogridAttributes]
  implicit val microgridReqData: OFormat[MicrogridRequestData]  = Json.format[MicrogridRequestData]
  implicit val microgridReq: OFormat[MicrogridRequest]          = Json.format[MicrogridRequest]
  implicit val microgridResData: OFormat[MicrogridResponseData] = Json.format[MicrogridResponseData]
  implicit val microgridRes: OFormat[MicrogridResponse]         = Json.format[MicrogridResponse]
  implicit val microgridCollRes: OFormat[MicrogridCollectionResponse] =
    Json.format[MicrogridCollectionResponse]

  implicit val groupId: OFormat[GroupIdentifier]      = Json.format[GroupIdentifier]
  implicit val groupIdColl: OFormat[GroupIdentifiers] = Json.format[GroupIdentifiers]
  implicit val groupRelReq: OFormat[GroupRelationshipsRequest] =
    Json.format[GroupRelationshipsRequest]
  implicit val groupAttrs: OFormat[GroupAttributes] = Json.format[GroupAttributes]
  implicit val groupData: OFormat[GroupData]        = Json.format[GroupData]
  implicit val groupReq: OFormat[GroupRequest]      = Json.format[GroupRequest]
  implicit val microgridsRel: OFormat[MicrogridsRelationships] =
    Json.format[MicrogridsRelationships]
  implicit val groupRelRes: OFormat[GroupRelationshipsResponse] =
    Json.format[GroupRelationshipsResponse]
  implicit val groupResData: OFormat[GroupResponseData] = Json.format[GroupResponseData]
  implicit val groupRes: OFormat[GroupResponse]         = Json.format[GroupResponse]
  implicit val groupCollectionRes: OFormat[GroupCollectionResponse] =
    Json.format[GroupCollectionResponse]

  implicit val subRelReq: OFormat[SubscriberRelationshipsRequest] =
    Json.format[SubscriberRelationshipsRequest]
  implicit val subAttrs: OFormat[SubscriberAttributes] = Json.format[SubscriberAttributes]
  implicit val subData: OFormat[SubscriberData]        = Json.format[SubscriberData]
  implicit val subReq: OFormat[SubscriberRequest]      = Json.format[SubscriberRequest]
  implicit val subsRel: OFormat[GroupsRelationships]   = Json.format[GroupsRelationships]
  implicit val subRelRes: OFormat[SubscriberRelationshipsResponse] =
    Json.format[SubscriberRelationshipsResponse]
  implicit val subResData: OFormat[SubscriberResponseData] = Json.format[SubscriberResponseData]
  implicit val subRes: OFormat[SubscriberResponse]         = Json.format[SubscriberResponse]
  implicit val subCollectionRes: OFormat[SubscriberCollectionResponse] =
    Json.format[SubscriberCollectionResponse]

  val ContentType: String = "application/vnd.api+json"
}
