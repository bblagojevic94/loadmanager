package io.mainflux.loadmanager.engine

import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit, TestProbe}
import io.mainflux.loadmanager.engine.Subscriptions.{AddMicrogrid, RemoveGroup, RemoveMicrogrid}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class GroupServiceSpec
    extends TestKit(ActorSystem())
    with WordSpecLike
    with ScalaFutures
    with MustMatchers
    with ImplicitSender
    with DefaultTimeout
    with MockitoSugar {

  "GroupService" when {
    "creating new group" should {
      "successfully save group" in new Fixture {
        when(groupRepository.save(any[Group])).thenReturn(Future.successful(group))
        when(microgridRepository.retrieveAllByIds(microgrids.map(_.id.get)))
          .thenReturn(Future.successful(microgrids))

        whenReady(groupService.create(group, microgrids.map(_.id.get))) { result =>
          result.id must be(group.id)
          result.name must be(group.name)
          (result.grids.map(_.id.get) must contain).allElementsOf(group.grids.map(_.id.get).seq)
        }
      }

      "store new group without specified grids that does not exists" in new Fixture {
        val grids: Seq[Long]                      = group.grids.map(_.id.get)
        val withoutFirstMicrogrid: Seq[Microgrid] = microgrids.tail

        when(groupRepository.save(any[Group]))
          .thenReturn(Future.successful(group.copy(grids = withoutFirstMicrogrid)))
        when(microgridRepository.retrieveAllByIds(grids)).thenReturn(Future.successful(withoutFirstMicrogrid))

        whenReady(groupService.create(group, grids)) { result =>
          result.name must be(group.name)
          (result.grids.map(_.id.get) must contain)
            .allElementsOf(withoutFirstMicrogrid.map(_.id.get))
          result.grids.map(_.id.get) must not(contain(microgrids.head.id.get))
        }
      }

      "should not save group without microgrids" in new Fixture {
        when(microgridRepository.retrieveAllByIds(group.grids.map(_.id.get)))
          .thenReturn(Future.successful(Seq()))

        an[IllegalArgumentException] must be thrownBy {
          Await.result(groupService.create(group, group.grids.map(_.id.get)), 1.second)
        }
      }
    }
    "retrieving one group" should {
      "successfully retrieve group" in new Fixture {
        when(groupRepository.retrieveOne(group.id.get)).thenReturn(Future.successful(Option(group)))

        whenReady(groupService.retrieveOne(group.id.get)) { result =>
          result.id must be(group.id)
          result.name must be(group.name)
          (result.grids.map(_.id.get) must contain).allElementsOf(group.grids.map(_.id.get).seq)
        }
      }
      "should throw exception if group does not exists" in new Fixture {
        when(groupRepository.retrieveOne(group.id.get)).thenReturn(Future.successful(None))

        an[EntityNotFound] must be thrownBy {
          Await.result(groupService.retrieveOne(group.id.get), 1.second)
        }
      }
    }
    "removing one group" should {
      "successfully remove group and send message to subscription actor" in new Fixture {
        when(groupRepository.remove(group.id.get)).thenReturn(Future.successful(1))

        groupService.remove(group.id.get)
        subscriptions.expectMsg(RemoveGroup(group.id.get))
      }
      "should throw exception if group does not exists" in new Fixture {
        when(groupRepository.remove(group.id.get)).thenReturn(Future.successful(0))

        an[EntityNotFound] must be thrownBy {
          Await.result(groupService.remove(group.id.get), 1.second)
        }
      }
    }
    "retrieving group microgrids" should {
      "successfully retrieve all microgrids" in new Fixture {
        when(groupRepository.retrieveOne(group.id.get)).thenReturn(Future.successful(Option(group)))
        when(microgridRepository.retrieveAllByGroup(group.id.get))
          .thenReturn(Future.successful(microgrids.map(_.id.get)))

        whenReady(groupService.retrieveGroupMicrogrids(group.id.get)) { result =>
          (result.seq must contain).allElementsOf(group.grids.map(_.id.get))
        }
      }
      "should throw exception if group does not exists" in new Fixture {
        when(groupRepository.retrieveOne(group.id.get)).thenReturn(Future.successful(None))

        an[EntityNotFound] must be thrownBy {
          Await.result(groupService.retrieveGroupMicrogrids(group.id.get), 1.second)
        }
      }
    }
    "adding microgrids to group" should {
      "successfully add microgrids and notify subscription actor" in new Fixture {
        when(groupRepository.retrieveOne(group.id.get)).thenReturn(Future.successful(Option(group)))
        when(groupRepository.addMicrogrids(group.id.get, microgrids.map(_.id.get)))
          .thenReturn(Future.successful(microgrids))
        when(groupRepository.hasSubscriptions(group.id.get)).thenReturn(Future.successful(true))

        groupService.addMicrogrids(group.id.get, microgrids.map(_.id.get))
        val messages: Seq[AddMicrogrid] = microgrids.map(mg => AddMicrogrid(mg, group.id.get))
        subscriptions.expectMsgAllOf(messages: _*)
      }
      "successfully add microgrids and does not send message to subscription actor" in new Fixture {
        when(groupRepository.retrieveOne(group.id.get)).thenReturn(Future.successful(Option(group)))
        when(groupRepository.addMicrogrids(group.id.get, microgrids.map(_.id.get)))
          .thenReturn(Future.successful(microgrids))
        when(groupRepository.hasSubscriptions(group.id.get)).thenReturn(Future.successful(false))

        groupService.addMicrogrids(group.id.get, microgrids.map(_.id.get))
        subscriptions.expectNoMessage(3.seconds)
      }
      "should throw exception if group does not exists" in new Fixture {
        when(groupRepository.retrieveOne(group.id.get)).thenReturn(Future.successful(None))

        an[EntityNotFound] must be thrownBy {
          Await.result(groupService.addMicrogrids(group.id.get, microgrids.map(_.id.get)), 1.second)
        }
      }
    }
    "removing microgrids from group" should {
      "successfully remove microgrid and notify subscription actor" in new Fixture {
        when(groupRepository.retrieveOne(group.id.get)).thenReturn(Future.successful(Option(group)))
        when(groupRepository.removeMicrogrids(group.id.get, microgrids.map(_.id.get)))
          .thenReturn(Future.successful(microgrids.map(_.id.get)))
        when(groupRepository.hasSubscriptions(group.id.get)).thenReturn(Future.successful(true))

        groupService.removeMicrogrids(group.id.get, microgrids.map(_.id.get))
        val messages: Seq[RemoveMicrogrid] =
          microgrids.map(mg => RemoveMicrogrid(mg.id.get, group.id.get))
        subscriptions.expectMsgAllOf(messages: _*)
      }
      "successfully add microgrids and does not send message to subscription actor" in new Fixture {
        when(groupRepository.retrieveOne(group.id.get)).thenReturn(Future.successful(Option(group)))
        when(groupRepository.removeMicrogrids(group.id.get, microgrids.map(_.id.get)))
          .thenReturn(Future.successful(microgrids.map(_.id.get)))
        when(groupRepository.hasSubscriptions(group.id.get)).thenReturn(Future.successful(false))

        groupService.removeMicrogrids(group.id.get, microgrids.map(_.id.get))
        subscriptions.expectNoMessage(3.seconds)
      }
      "should throw exception if group does not exists" in new Fixture {
        when(groupRepository.retrieveOne(group.id.get)).thenReturn(Future.successful(None))

        an[EntityNotFound] must be thrownBy {
          Await.result(groupService.removeMicrogrids(group.id.get, microgrids.map(_.id.get)), 1.second)
        }
      }
    }
  }

  trait Fixture {
    val groupRepository: GroupRepository         = mock[GroupRepository]
    val microgridRepository: MicrogridRepository = mock[MicrogridRepository]
    val subscriptions: TestProbe                 = TestProbe()

    val groupService = new GroupService(groupRepository, microgridRepository, subscriptions.ref)

    val microgrids: Seq[Microgrid] = (0 until 3).map(createMicrogrid)
    val group: Group               = createGroup(1, microgrids)
  }

  def createMicrogrid(seed: Int): Microgrid =
    Microgrid(id = Some(seed),
              url = s"test-url-$seed",
              platform = Platform.OSGP,
              organisationId = seed.toString)

  def createGroup(seed: Int, grids: Seq[Microgrid]): Group =
    Group(Some(seed), s"group-$seed", grids = grids)

}
