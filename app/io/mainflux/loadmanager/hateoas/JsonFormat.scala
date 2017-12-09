package io.mainflux.loadmanager.hateoas

import play.api.libs.json.{Json, OFormat}

trait JsonFormat {

  val ContentType: String = "application/vnd.api+json"

  implicit val links: OFormat[Links]            = Json.format[Links]
  implicit val meta: OFormat[Meta]              = Json.format[Meta]
  implicit val error: OFormat[Error]            = Json.format[Error]
  implicit val errorRes: OFormat[ErrorResponse] = Json.format[ErrorResponse]

  implicit val microgridId: OFormat[MicrogridIdentifier]        = Json.format[MicrogridIdentifier]
  implicit val microgridIdColl: OFormat[MicrogridIdentifiers]   = Json.format[MicrogridIdentifiers]
  implicit val microgridAttrs: OFormat[MicrogridAttributes]     = Json.format[MicrogridAttributes]
  implicit val microgridReqData: OFormat[MicrogridRequestData]  = Json.format[MicrogridRequestData]
  implicit val microgridReq: OFormat[MicrogridRequest]          = Json.format[MicrogridRequest]
  implicit val microgridResData: OFormat[MicrogridResponseData] = Json.format[MicrogridResponseData]
  implicit val microgridRes: OFormat[MicrogridResponse]         = Json.format[MicrogridResponse]
  implicit val microgridCollRes: OFormat[MicrogridCollectionResponse] =
    Json.format[MicrogridCollectionResponse]

  implicit val groupId: OFormat[GroupIdentifier]                    = Json.format[GroupIdentifier]
  implicit val groupIdColl: OFormat[GroupIdentifiers]               = Json.format[GroupIdentifiers]
  implicit val groupRelReq: OFormat[GroupRelationshipsRequest]      = Json.format[GroupRelationshipsRequest]
  implicit val groupAttrs: OFormat[GroupAttributes]                 = Json.format[GroupAttributes]
  implicit val groupData: OFormat[GroupData]                        = Json.format[GroupData]
  implicit val groupReq: OFormat[GroupRequest]                      = Json.format[GroupRequest]
  implicit val microgridsRel: OFormat[MicrogridsRelationships]      = Json.format[MicrogridsRelationships]
  implicit val groupRelRes: OFormat[GroupRelationshipsResponse]     = Json.format[GroupRelationshipsResponse]
  implicit val groupResData: OFormat[GroupResponseData]             = Json.format[GroupResponseData]
  implicit val groupRes: OFormat[GroupResponse]                     = Json.format[GroupResponse]
  implicit val groupCollectionRes: OFormat[GroupCollectionResponse] = Json.format[GroupCollectionResponse]

  implicit val subRelReq: OFormat[SubscriptionRelationshipsRequest] =
    Json.format[SubscriptionRelationshipsRequest]
  implicit val subAttrs: OFormat[SubscriptionAttributes] = Json.format[SubscriptionAttributes]
  implicit val subData: OFormat[SubscriptionData]        = Json.format[SubscriptionData]
  implicit val subReq: OFormat[SubscriptionRequest]      = Json.format[SubscriptionRequest]
  implicit val subsRel: OFormat[GroupsRelationships]     = Json.format[GroupsRelationships]
  implicit val subRelRes: OFormat[SubscriptionRelationshipsResponse] =
    Json.format[SubscriptionRelationshipsResponse]
  implicit val subResData: OFormat[SubscriptionResponseData] = Json.format[SubscriptionResponseData]
  implicit val subRes: OFormat[SubscriptionResponse]         = Json.format[SubscriptionResponse]
  implicit val subCollectionRes: OFormat[SubscriptionCollectionResponse] =
    Json.format[SubscriptionCollectionResponse]

}
