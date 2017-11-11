package io.mainflux.loadmanager.hateoas

import java.time.LocalDateTime

import io.mainflux.loadmanager.engine.Group

case class GroupRequest(data: GroupData)

case class GroupData(`type`: String, attributes: GroupAttributes, relationships: GroupRelationshipsRequest) {
  def toDomain: (Group, Seq[Long]) =
    (Group(name = attributes.name, createdAt = LocalDateTime.now()), relationships.microgrids.data.map(_.id))
}

case class GroupAttributes(name: String)

case class GroupRelationshipsRequest(microgrids: MicrogridIdentifiers)

case class GroupResponse(data: GroupResponseData)

object GroupResponse {
  def fromDomain(group: Group) =
    GroupResponse(GroupResponseData.fromDomain(group))
}

case class GroupResponseData(`type`: String,
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

case class GroupRelationshipsResponse(microgrids: MicrogridsRelationships)

case class MicrogridsRelationships(links: Links, data: Seq[MicrogridIdentifier])

case class Links(self: String)
