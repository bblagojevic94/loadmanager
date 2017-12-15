package io.mainflux.loadmanager.hateoas

import java.time.LocalDateTime

import io.mainflux.loadmanager.engine.{Group, GroupInfo}
import org.scalatest.{FlatSpec, MustMatchers}

class GroupSpec extends FlatSpec with MustMatchers {

  "Group entity" must "be created from given valid request" in {
    val attributes = GroupAttributes("test-name")
    val relationships = GroupRelationshipsRequest(
      MicrogridIdentifiers(
        Set(MicrogridIdentifier(MicrogridType, 1),
            MicrogridIdentifier(MicrogridType, 2),
            MicrogridIdentifier(MicrogridType, 3))
      )
    )
    val request = GroupRequest(GroupData(GroupType, attributes, relationships))

    val group = request.data.toDomain
    group.info.name must be("test-name")
    (group.microgrids must contain).allElementsOf(relationships.microgrids.data.map(_.id))
  }

  it must "not be created from request with invalid data type provided" in {
    val attributes = GroupAttributes("test-name")
    val relationships =
      GroupRelationshipsRequest(MicrogridIdentifiers(Set(MicrogridIdentifier(MicrogridType, 1))))

    an[IllegalArgumentException] must be thrownBy {
      GroupRequest(GroupData("invalid-type", attributes, relationships))
    }
  }

  it must "not be created from request that does not contain at least one microgrid" in {
    an[IllegalArgumentException] must be thrownBy {
      GroupRelationshipsRequest(MicrogridIdentifiers(Set()))
    }
  }

  "Group request" must "be created from group entity" in {
    val g        = group(1)
    val response = GroupResponse.fromDomain(g)

    response.data must have(
      'id (g.info.id),
      'type (GroupType)
    )

    response.data.attributes.name must be(g.info.name)
    (response.data.relationships.microgrids.data.map(_.id) must contain).allElementsOf(g.microgrids)
    response.data.relationships.microgrids.links.self must be(
      s"/$GroupType/1/relationships/$MicrogridType"
    )
    response.data.links.self must be(s"/$GroupType/1")
  }

  "Group collection response" must "be created from given sequence of groups" in {
    val groups   = (0 until 3).map(_.toLong).map(group)
    val response = GroupCollectionResponse.fromDomain(groups)

    (response.data.map(_.id) must contain).allElementsOf(groups.map(_.info.id))
    response.data.foreach(_.relationships.microgrids.data.size must be(2))
  }

  "Response containing group identifiers" must "be created from given sequence of groups" in {
    val ids      = (0 until 3).map(_.toLong).toSet
    val response = GroupIdentifiers.fromDomain(ids)

    (response.data.map(_.id) must contain).allElementsOf(ids)
    all(response.data.map(_.`type`)) must be(GroupType)
  }

  "Sequence of group ids" must "be created from valid request" in {
    val gIds = GroupIdentifiers(
      (0 until 3).map(id => GroupIdentifier(GroupType, id.toLong)).toSet
    )
    val ids = gIds.toDomain

    (gIds.data.map(_.id) must contain).allElementsOf(ids)
  }

  private def group(seed: Long) =
    Group(GroupInfo(Some(seed), s"name-$seed", LocalDateTime.now()),
          (0 until 2).map(_.toLong).toSet)

}
