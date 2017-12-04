package io.mainflux.loadmanager.engine

import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit, TestProbe}
import io.mainflux.loadmanager.engine.Subscriptions.{RemoveSubscriber, SubscribeOnGroup, UnsubscribeFromGroup}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class SubscriptionServiceSpec
    extends TestKit(ActorSystem())
    with WordSpecLike
    with ScalaFutures
    with MustMatchers
    with ImplicitSender
    with DefaultTimeout
    with MockitoSugar {

  "SubscriptionService" when {
    "creating new subscription" should {
      "successfully save subscription and send message to subscription actor" in new Fixture {
        when(subscriptionRepository.save(any[Subscription])).thenReturn(Future.successful(subscription))
        when(groupRepository.retrieveAll(groups.map(_.id.get).toSet)).thenReturn(Future.successful(groups))

        whenReady(subscriptionService.create(subscription)) { result =>
          result.id must be(subscription.id)
          result.callback must be(subscription.callback)
          (result.groupIds must contain).allElementsOf(groups.map(_.id.get))

          val messages = result.groupIds.map(gId => SubscribeOnGroup(gId, subscription))
          subscriptions.expectMsgAllOf(messages: _*)
        }
      }

      "should not save subscription without subscribed groups" in new Fixture {
        when(groupRepository.retrieveAll(groups.map(_.id.get).toSet)).thenReturn(Future.successful(Seq()))

        an[IllegalArgumentException] must be thrownBy {
          Await.result(subscriptionService.create(subscription), 1.second)
        }
      }
    }
    "retrieving one subscription" should {
      "successfully retrieve subscription" in new Fixture {
        when(subscriptionRepository.retrieveOne(subscription.id.get))
          .thenReturn(Future.successful(Option(subscription)))

        whenReady(subscriptionService.retrieveOne(subscription.id.get)) { result =>
          result.id must be(subscription.id)
          result.callback must be(subscription.callback)
          (result.groupIds must contain).allElementsOf(groups.map(_.id.get))
        }
      }
      "should throw exception if group does not exists" in new Fixture {
        when(subscriptionRepository.retrieveOne(subscription.id.get)).thenReturn(Future.successful(None))

        an[EntityNotFound] must be thrownBy {
          Await.result(subscriptionService.retrieveOne(subscription.id.get), 1.second)
        }
      }
    }
    "removing one subscription" should {
      "successfully remove subscription and send message to subscription actor" in new Fixture {
        when(subscriptionRepository.remove(subscription.id.get)).thenReturn(Future.successful(1))

        subscriptionService.remove(subscription.id.get)
        subscriptions.expectMsg(RemoveSubscriber(subscription.id.get))
      }
      "should throw exception if group does not exists" in new Fixture {
        when(subscriptionRepository.remove(subscription.id.get)).thenReturn(Future.successful(0))

        an[EntityNotFound] must be thrownBy {
          Await.result(subscriptionService.remove(subscription.id.get), 1.second)
        }
      }
    }
    "retrieving subscription groups" should {
      "successfully retrieve all groups" in new Fixture {
        when(subscriptionRepository.retrieveOne(subscription.id.get))
          .thenReturn(Future.successful(Option(subscription)))
        when(groupRepository.retrieveAllBySubscription(subscription.id.get))
          .thenReturn(Future.successful(groups.map(_.id.get)))

        whenReady(subscriptionService.retrieveSubscriberGroups(subscription.id.get)) { result =>
          (result.seq must contain).allElementsOf(groups.map(_.id.get))
        }
      }
      "should throw exception if subscription does not exists" in new Fixture {
        when(subscriptionRepository.retrieveOne(subscription.id.get)).thenReturn(Future.successful(None))

        an[EntityNotFound] must be thrownBy {
          Await.result(subscriptionService.retrieveSubscriberGroups(subscription.id.get), 1.second)
        }
      }
    }
    "subscribing on group" should {
      "successfully subscribe and notify subscription actor" in new Fixture {
        when(subscriptionRepository.retrieveOne(subscription.id.get))
          .thenReturn(Future.successful(Option(subscription)))
        when(subscriptionRepository.subscribeOnGroups(subscription.id.get, groups.map(_.id.get)))
          .thenReturn(Future.successful(groups.map(_.id.get)))

        subscriptionService.subscribeOnGroup(subscription.id.get, groups.map(_.id.get))
        val messages: Seq[SubscribeOnGroup] =
          groups.map(g => SubscribeOnGroup(g.id.get, subscription))
        subscriptions.expectMsgAllOf(messages: _*)
      }
      "should throw exception if subscription does not exists" in new Fixture {
        when(subscriptionRepository.retrieveOne(subscription.id.get)).thenReturn(Future.successful(None))

        an[EntityNotFound] must be thrownBy {
          Await.result(subscriptionService.subscribeOnGroup(subscription.id.get, groups.map(_.id.get)),
                       1.second)
        }
      }
    }
    "unsubscribing from group" should {
      "successfully notify subscription actor" in new Fixture {
        when(subscriptionRepository.retrieveOne(subscription.id.get))
          .thenReturn(Future.successful(Option(subscription)))
        when(subscriptionRepository.unsubscribeFromGroups(subscription.id.get, groups.map(_.id.get)))
          .thenReturn(Future.successful(groups.map(_.id.get)))

        subscriptionService.unsubscribeFromGroup(subscription.id.get, groups.map(_.id.get))
        val messages: Seq[UnsubscribeFromGroup] =
          groups.map(g => UnsubscribeFromGroup(subscription.id.get, g.id.get))
        subscriptions.expectMsgAllOf(messages: _*)
      }
      "should throw exception if group does not exists" in new Fixture {
        when(subscriptionRepository.retrieveOne(subscription.id.get)).thenReturn(Future.successful(None))

        an[EntityNotFound] must be thrownBy {
          Await.result(subscriptionService.unsubscribeFromGroup(subscription.id.get, groups.map(_.id.get)),
                       1.second)
        }
      }
    }
  }

  trait Fixture {
    val groupRepository: GroupRepository               = mock[GroupRepository]
    val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]
    val subscriptions: TestProbe                       = TestProbe()

    val subscriptionService =
      new SubscriptionService(subscriptionRepository, groupRepository, subscriptions.ref)

    val microgrids: Seq[Microgrid] = (0 until 5).map(createMicrogrid)
    val group1: Group              = createGroup(1, microgrids.take(3))
    val group2: Group              = createGroup(2, microgrids.drop(3))
    val groups                     = Seq(group1, group2)
    val subscription: Subscription = createSubscription(1, groups.map(_.id.get))
  }

  def createSubscription(seed: Int, groupIds: Seq[Long]): Subscription =
    Subscription(Some(seed), callback = s"clb-$seed", groupIds = groupIds)

  def createMicrogrid(seed: Int): Microgrid =
    Microgrid(id = Some(seed),
              url = s"test-url-$seed",
              platform = Platform.OSGP,
              organisationId = seed.toString)

  def createGroup(seed: Int, grids: Seq[Microgrid]): Group =
    Group(Some(seed), s"group-$seed", grids = grids)

}
