package io.mainflux.loadmanager.hateoas

import io.mainflux.loadmanager.engine.Subscription

final case class SubscriptionRequest(data: SubscriptionData)

final case class SubscriptionData(`type`: String,
                                  attributes: SubscriptionAttributes,
                                  relationships: SubscriptionRelationshipsRequest) {
  def toDomain: Subscription =
    Subscription(callback = attributes.callback, groupIds = relationships.groups.data.map(_.id))
}

final case class SubscriptionAttributes(callback: String)

final case class SubscriptionRelationshipsRequest(groups: GroupIdentifiers)

final case class SubscriptionResponse(data: SubscriptionResponseData)

object SubscriptionResponse {
  def fromDomain(subscription: Subscription) =
    SubscriptionResponse(SubscriptionResponseData.fromDomain(subscription))
}

final case class SubscriptionResponseData(`type`: String,
                                          id: Long,
                                          attributes: SubscriptionAttributes,
                                          relationships: SubscriptionRelationshipsResponse,
                                          links: Links)

object SubscriptionResponseData {
  def fromDomain(subscription: Subscription): SubscriptionResponseData = {
    val relationships =
      SubscriptionRelationshipsResponse(
        GroupsRelationships(
          Links(s"/$SubscriberType/${subscription.id.get}/relationships/$GroupType"),
          subscription.groupIds.map { groupId =>
            GroupIdentifier(GroupType, groupId)
          }
        )
      )
    SubscriptionResponseData(
      SubscriberType,
      subscription.id.get,
      SubscriptionAttributes(callback = subscription.callback),
      relationships,
      Links(s"/$SubscriberType/${subscription.id.get}")
    )
  }
}

final case class SubscriptionRelationshipsResponse(groups: GroupsRelationships)

final case class GroupsRelationships(links: Links, data: Seq[GroupIdentifier])

final case class SubscriptionCollectionResponse(data: Seq[SubscriptionResponseData])

object SubscriptionCollectionResponse {
  def fromDomain(subscriptions: Seq[Subscription]): SubscriptionCollectionResponse = {
    val data: Seq[SubscriptionResponseData] = subscriptions.map(SubscriptionResponseData.fromDomain)
    SubscriptionCollectionResponse(data)
  }
}
