package io.mainflux.loadmanager.hateoas

import io.mainflux.loadmanager.engine.model.{Group, Microgrid, Platform}
import org.scalatest.{MustMatchers, WordSpecLike}

class GroupSpec extends WordSpecLike with MustMatchers {

  "GroupHateoas" when {
    "parsing group request" should {
      "create valid Group" in {
        val attributes = GroupAttributes(name = "test-name")
        val relationships = GroupRelationshipsRequest(
          MicroGridIdentifierCollection(
            Seq(MicroGridIdentifier(MicrogridType, 1),
                MicroGridIdentifier(MicrogridType, 2),
                MicroGridIdentifier(MicrogridType, 3))
          )
        )
        val request = GroupRequest(GroupData(GroupType, attributes, relationships))

        val (group, microgrids) = request.data.toDomain
        group.name must be("test-name")
        (microgrids must contain).allElementsOf(relationships.microgrids.data.map(_.id))
      }
    }

    "creating group response" should {
      "create valid response" in {
        val microgrids = Seq(
          Microgrid(Option(1), "test-url-1", Platform.OSGP, "org-1"),
          Microgrid(Option(2), "test-url-2", Platform.MAINFLUX, "org-2"),
          Microgrid(Option(3), "test-url-3", Platform.OSGP, "org-3")
        )
        val group = Group(Option(1), "test-name", microgrids)

        val response = GroupResponse.fromDomain(group)

        response.data must have(
          'id (1),
          'type (GroupType)
        )

        response.data.attributes.name must be("test-name")

        (response.data.relationships.microgrids.data.map(mg => Option(mg.id)) must contain)
          .allElementsOf(microgrids.map(_.id))

        response.data.relationships.microgrids.links.self must be(
          s"/$GroupType/1/relationships/$MicrogridType"
        )
        response.data.links.self must be(s"/$GroupType/1")
      }
    }
  }
}
