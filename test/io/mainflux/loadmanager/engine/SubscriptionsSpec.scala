package io.mainflux.loadmanager.engine

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit, TestProbe}
import io.mainflux.loadmanager.engine.Subscriptions.{AddMicrogrid, RemoveGroup, Tick}
import io.mainflux.loadmanager.osgp.Worker.CalculateLoad
import org.mockito.Mockito.when
import org.scalatest._
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

class SubscriptionsSpec
    extends TestKit(ActorSystem())
    with WordSpecLike
    with MustMatchers
    with BeforeAndAfterAll
    with ImplicitSender
    with DefaultTimeout
    with MockitoSugar {

  val random: Random = scala.util.Random
  val numberOfGroups = 5

  val groupRepository: GroupRepository               = mock[GroupRepository]
  val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

  def createMicrogrid(seed: Int): Microgrid =
    Microgrid(id = Some(seed),
              url = s"test-url-$seed",
              platform = Platform.OSGP,
              organisationId = seed.toString)

  def createGroup(seed: Int): Group =
    Group(Some(seed), s"group-$seed", grids = (0 until 3).map(createMicrogrid))

  def createSubscription(seed: Int, subGroupsCount: Int): Subscription =
    Subscription(Some(seed),
                 callback = s"clb-$seed",
                 groupIds = (0 until 3).map(_ => random.nextInt(subGroupsCount).toLong))

  val groups: Seq[Group]               = (0 until numberOfGroups).map(createGroup)
  val subscriptions: Seq[Subscription] = (0 until 3).map(n => createSubscription(n, numberOfGroups))
  val subscribedGroupsIds: Set[Long] = subscriptions.flatMap(_.groupIds).distinct.toSet
  val subscribedGroups: Seq[Group] = groups.filter(g => subscribedGroupsIds.contains(g.id.get))

  when(groupRepository.retrieveAll(subscribedGroupsIds)).thenReturn(Future.successful(subscribedGroups))
  when(subscriptionRepository.retrieveAll).thenReturn(Future.successful(subscriptions))

  val messages: Seq[CalculateLoad] =
    subscribedGroups.flatMap { group =>
      group.grids.map { mg =>
        CalculateLoad(group.id.get, mg)
      }
    }

  val osgpWorker                             = TestProbe()
  var subsActor: TestActorRef[Subscriptions] = _

  override def beforeAll: Unit =
    subsActor = TestActorRef(
      Props(new Subscriptions(groupRepository, subscriptionRepository) {
        override val osgpWorkers: ActorRef = osgpWorker.ref
      }).withDispatcher("akka.actor.stash-dispatcher"),
      "subscription"
    )

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "Subscriptions actor" when {
    "initialize actor state" must {
      "successfully create state from db" in {
        subsActor ! Tick
        osgpWorker.expectMsgAllOf(messages: _*)
      }
    }

    "add microgrid to actor state" must {
      "calculate load for same" in {
        val microgrid: Microgrid = createMicrogrid(100)
        val groupId: Long        = subscribedGroups.head.id.get

        subsActor ! AddMicrogrid(microgrid, groupId = groupId)
        subsActor ! Tick

        osgpWorker.expectMsgAllOf(messages :+ CalculateLoad(groupId, microgrid): _*)
      }
    }

    "remove group from actor state" must {
      "not calculate load for same" in {
        val groupId: Long = subscribedGroups.head.id.get

        subsActor ! RemoveGroup(groupId)
        subsActor ! Tick

        osgpWorker.expectMsgAllOf(messages.filterNot(_.groupId == groupId): _*)
      }
    }
  }
}
