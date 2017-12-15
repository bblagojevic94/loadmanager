package io.mainflux.loadmanager.hateoas

import java.time.LocalDateTime

import io.mainflux.loadmanager.engine.{Group, GroupInfo}
import org.apache.commons.lang3.StringUtils.EMPTY

final case class GroupCollectionResponse(data: Seq[GroupResponseData])

object GroupCollectionResponse {
  def fromDomain(groups: Seq[Group]): GroupCollectionResponse = {
    val data: Seq[GroupResponseData] = groups.map(GroupResponseData.fromDomain)
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
    val groupLink: String = s"/$GroupType/${group.info.id.map(_.toString).getOrElse(EMPTY)}"

    val relationships =
      GroupRelationshipsResponse(
        MicrogridsRelationships(
          links = Links(s"/$groupLink/relationships/$MicrogridType"),
          data = group.microgrids.map(mg => MicrogridIdentifier(MicrogridType, mg))
        )
      )

    GroupResponseData(GroupType,
                      group.info.id,
                      GroupAttributes(name = group.info.name),
                      relationships,
                      Links(groupLink))
  }
}

final case class GroupAttributes(name: String)

final case class GroupRelationshipsResponse(microgrids: MicrogridsRelationships)

final case class MicrogridsRelationships(links: Links, data: Set[MicrogridIdentifier])

final case class GroupRequest(data: GroupData)

final case class GroupData(`type`: String,
                           attributes: GroupAttributes,
                           relationships: GroupRelationshipsRequest) {
  def toDomain: Group =
    Group(info = GroupInfo(id = None, name = attributes.name, createdAt = LocalDateTime.now()),
          microgrids = relationships.microgrids.data.map(_.id))
}

final case class GroupRelationshipsRequest(microgrids: MicrogridIdentifiers)

final case class GroupResponse(data: GroupResponseData)

object GroupResponse {
  def fromDomain(group: Group) =
    GroupResponse(GroupResponseData.fromDomain(group))
}

final case class GroupIdentifier(`type`: String, id: Long)

final case class GroupIdentifiers(data: Set[GroupIdentifier]) {
  def toDomain: Set[Long] = data.map(_.id)
}

object GroupIdentifiers {
  def fromDomain(groups: Set[Long]): GroupIdentifiers =
    GroupIdentifiers(groups.map { groupId =>
      GroupIdentifier(GroupType, groupId)
    })
}
