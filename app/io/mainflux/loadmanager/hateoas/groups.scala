package io.mainflux.loadmanager.hateoas

import java.time.LocalDateTime

import io.mainflux.loadmanager.controllers.routes
import io.mainflux.loadmanager.engine.{Group, GroupInfo}

final case class GroupCollectionResponse(data: Seq[GroupResponseData])

object GroupCollectionResponse {
  def fromDomain(groups: Seq[Group]): GroupCollectionResponse = {
    val data = groups.map(GroupResponseData.fromDomain)
    GroupCollectionResponse(data)
  }
}

final case class GroupResponseData(`type`: String,
                                   id: Option[Long],
                                   attributes: GroupAttributes,
                                   relationships: GroupRelationshipsResponse,
                                   links: Links)

object GroupResponseData {
  def fromDomain(group: Group): GroupResponseData = {
    val relationships =
      GroupRelationshipsResponse(
        MicrogridsRelationships(
          Links(group.info.id.fold("")(id => routes.Groups.retrieveMicrogrids(id).url)),
          group.microgrids.map(mg => MicrogridIdentifier(MicrogridType, mg))
        )
      )

    GroupResponseData(GroupType,
                      group.info.id,
                      GroupAttributes(group.info.name),
                      relationships,
                      Links(group.info.id.fold("")(id => routes.Groups.retrieveOne(id).url)))
  }
}

final case class GroupAttributes(name: String)

final case class GroupRelationshipsResponse(microgrids: MicrogridsRelationships)

final case class MicrogridsRelationships(links: Links, data: Set[MicrogridIdentifier])

final case class GroupRequest(data: GroupData)

final case class GroupData(`type`: String,
                           attributes: GroupAttributes,
                           relationships: GroupRelationshipsRequest) {
  require(`type`.equalsIgnoreCase(GroupType), s"Invalid type ${`type`} provided.")

  def toDomain: Group =
    Group(GroupInfo(None, attributes.name, LocalDateTime.now()),
          relationships.microgrids.data.map(_.id))
}

final case class GroupRelationshipsRequest(microgrids: MicrogridIdentifiers) {
  require(microgrids.data.nonEmpty, s"You must specify at least one microgrid.")
}

final case class GroupResponse(data: GroupResponseData)

object GroupResponse {
  def fromDomain(group: Group) =
    GroupResponse(GroupResponseData.fromDomain(group))
}

final case class GroupIdentifier(`type`: String, id: Long) {
  require(`type`.equalsIgnoreCase(GroupType), s"Invalid type ${`type`} provided.")
}

final case class GroupIdentifiers(data: Set[GroupIdentifier]) {
  def toDomain: Set[Long] = data.map(_.id)
}

object GroupIdentifiers {
  def fromDomain(groups: Set[Long]): GroupIdentifiers =
    GroupIdentifiers(groups.map { groupId =>
      GroupIdentifier(GroupType, groupId)
    })
}
