package io.mainflux.loadmanager.hateoas

import java.time.LocalDateTime

import io.mainflux.loadmanager.controllers.routes
import io.mainflux.loadmanager.engine.{Subscriber, SubscriberInfo}

final case class SubscriberCollectionResponse(data: Seq[SubscriberResponseData])

object SubscriberCollectionResponse {
  def fromDomain(subscribers: Seq[Subscriber]): SubscriberCollectionResponse =
    SubscriberCollectionResponse(subscribers.map(SubscriberResponseData.fromDomain))
}

final case class SubscriberResponseData(`type`: String,
                                        id: Option[Long],
                                        attributes: SubscriberAttributes,
                                        relationships: SubscriberRelationshipsResponse,
                                        links: Links)

object SubscriberResponseData {
  def fromDomain(subscriber: Subscriber): SubscriberResponseData = {
    val relationships =
      SubscriberRelationshipsResponse(
        GroupsRelationships(
          Links(subscriber.info.id.fold("")(id => routes.Subscribers.retrieveGroups(id).url)),
          subscriber.groups.map { groupId =>
            GroupIdentifier(GroupType, groupId)
          }
        )
      )

    SubscriberResponseData(
      SubscriberType,
      subscriber.info.id,
      SubscriberAttributes(subscriber.info.callback),
      relationships,
      Links(subscriber.info.id.fold("")(id => routes.Subscribers.retrieveOne(id).url))
    )
  }
}

final case class SubscriberAttributes(callback: String)

final case class SubscriberRelationshipsResponse(groups: GroupsRelationships)

final case class GroupsRelationships(links: Links, data: Set[GroupIdentifier])

final case class SubscriberRequest(data: SubscriberData)

final case class SubscriberData(`type`: String,
                                attributes: SubscriberAttributes,
                                relationships: SubscriberRelationshipsRequest) {
  require(`type`.equalsIgnoreCase(SubscriberType), s"Invalid type ${`type`} provided.")

  def toDomain: Subscriber =
    Subscriber(SubscriberInfo(None, attributes.callback, LocalDateTime.now()),
               relationships.groups.data.map(_.id))
}

final case class SubscriberRelationshipsRequest(groups: GroupIdentifiers) {
  require(groups.data.nonEmpty, s"You must specify at least one group.")
}

final case class SubscriberResponse(data: SubscriberResponseData)

object SubscriberResponse {
  def fromDomain(subscriber: Subscriber) =
    SubscriberResponse(SubscriberResponseData.fromDomain(subscriber))
}
