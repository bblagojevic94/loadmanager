package io.mainflux.loadmanager.hateoas

import java.time.LocalDateTime

import io.mainflux.loadmanager.engine.Group

final case class GroupRequest(data: GroupData)

final case class GroupData(`type`: String,
                           attributes: GroupAttributes,
                           relationships: GroupRelationshipsRequest) {
  def toDomain: (Group, Seq[Long]) =
    (Group(name = attributes.name, createdAt = LocalDateTime.now()), relationships.microgrids.data.map(_.id))
}

final case class GroupAttributes(name: String)

final case class GroupRelationshipsRequest(microgrids: MicrogridIdentifiers)

final case class GroupResponse(data: GroupResponseData)

object GroupResponse {
  def fromDomain(group: Group) =
    GroupResponse(GroupResponseData.fromDomain(group))
}

final case class GroupResponseData(`type`: String,
                                   id: Long,
                                   attributes: GroupAttributes,
                                   relationships: GroupRelationshipsResponse,
                                   links: Links)

object GroupResponseData {
  def fromDomain(group: Group): GroupResponseData = {
    val relationships =
      GroupRelationshipsResponse(
        MicrogridsRelationships(
          Links(s"/$GroupType/${group.id.get}/relationships/$MicrogridType"),
          group.grids.map(mg => MicrogridIdentifier(MicrogridType, mg.id.get))
        )
      )
    GroupResponseData(
      GroupType,
      group.id.get,
      GroupAttributes(name = group.name),
      relationships,
      Links(s"/$GroupType/${group.id.get}")
    )
  }
}

final case class GroupRelationshipsResponse(microgrids: MicrogridsRelationships)

final case class MicrogridsRelationships(links: Links, data: Seq[MicrogridIdentifier])

final case class Links(self: String)

final case class GroupCollectionResponse(data: Seq[GroupResponseData])

object GroupCollectionResponse {
  def fromDomain(groups: Seq[Group]): GroupCollectionResponse = {
    val data: Seq[GroupResponseData] = groups.map(GroupResponseData.fromDomain)
    GroupCollectionResponse(data)
  }
}
