package io.mainflux.loadmanager.hateoas

import java.time.LocalDateTime

import io.mainflux.loadmanager.UnitSpec
import io.mainflux.loadmanager.engine._

final class SubscriberSpec extends UnitSpec {
  "Subscriber entity" must "be created from valid request" in {
    val attributes = SubscriberAttributes("test-callback")

    val groups        = (1 to 3).map(id => GroupIdentifier(GroupType, id))
    val relationships = SubscriberRelationshipsRequest(GroupIdentifiers(groups.toSet))

    val request = SubscriberRequest(SubscriberData(SubscriberType, attributes, relationships))

    val subscriber = request.data.toDomain
    subscriber.info.callback should be("test-callback")
    (subscriber.groups should contain).allElementsOf(relationships.groups.data.map(_.id))
  }

  it should "not be created from request with invalid data type provided" in {
    val attributes = SubscriberAttributes("test-callback")
    val relationships =
      SubscriberRelationshipsRequest(GroupIdentifiers(Set(GroupIdentifier(GroupType, 1))))

    an[IllegalArgumentException] should be thrownBy {
      SubscriberRequest(SubscriberData("invalid-type", attributes, relationships))
    }
  }

  it should "not be created from request that does not contain at least one group" in {
    an[IllegalArgumentException] should be thrownBy {
      SubscriberRelationshipsRequest(GroupIdentifiers(Set()))
    }
  }

  "Subscriber response representation" must "be created from subscriber entity" in {
    val s        = subscriber(1)
    val response = SubscriberResponse.fromDomain(s)

    response.data should have(
      'id (s.info.id),
      'type (SubscriberType)
    )

    response.data.attributes.callback should be(s.info.callback)
    (response.data.relationships.groups.data.map(_.id) should contain).allElementsOf(s.groups)
    response.data.relationships.groups.links.self should be(
      s"/$SubscriberType/1/relationships/$GroupType"
    )
    response.data.links.self should be(s"/$SubscriberType/1")
  }

  "Subscriber collection response" should "be created from sequence of subscribers" in {
    val subscribers = (0 until 3).map(_.toLong).map(subscriber)
    val response    = SubscriberCollectionResponse.fromDomain(subscribers)

    (response.data.map(_.id) should contain).allElementsOf(subscribers.map(_.info.id))
    response.data.foreach(_.relationships.groups.data.size should be(2))
  }

  private def subscriber(id: Long) = {
    val info   = SubscriberInfo(Some(id), s"test-callback-$id", LocalDateTime.now())
    val groups = (0 until 2).map(_.toLong).toSet

    Subscriber(info, groups)
  }
}
