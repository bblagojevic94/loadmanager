package io.mainflux.loadmanager.hateoas

import io.mainflux.loadmanager.engine.model.Group

case class GroupRequest(data: GroupData)

case class GroupData(`type`: String, attributes: GroupAttributes, relationships: GroupRelationshipsRequest) {
  def toDomain: (Group, Seq[Long]) =
    (Group(name = attributes.name), relationships.microgrids.data.map(_.id))
}

case class GroupAttributes(name: String)

case class GroupRelationshipsRequest(microgrids: MicroGridIdentifierCollection)

case class GroupResponse(data: GroupResponseData)

object GroupResponse {
  def fromDomain(group: Group) =
    GroupResponse(GroupResponseData.fromDomain(group))
}

case class GroupResponseData(`type`: String,
                             id: Long,
                             attributes: GroupAttributes,
                             relationships: GroupRelationshipsResponse)

object GroupResponseData {
  def fromDomain(group: Group): GroupResponseData = {
    val relationships =
      GroupRelationshipsResponse(
        Links(s"/$GroupType/${group.id.get}"),
        MicroGridIdentifierCollection(group.grids.map(mg => MicroGridIdentifier(MicroGridType, mg.id.get)))
      )
    GroupResponseData(
      GroupType,
      group.id.get,
      GroupAttributes(name = group.name),
      relationships
    )
  }
}

case class GroupRelationshipsResponse(links: Links, microgrids: MicroGridIdentifierCollection)

case class Links(self: String)
