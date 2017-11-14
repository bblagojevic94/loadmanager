package io.mainflux.loadmanager.engine.service

import io.mainflux.loadmanager.engine.Platform._
import io.mainflux.loadmanager.engine._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class GroupServiceSpec extends WordSpecLike with MustMatchers with MockitoSugar with ScalaFutures {

  "GroupService" must {
    "store new group" in new Fixture {
      val grids: Seq[Long] = microgrids.map(_.id.get)
      when(groupRepository.save(group)).thenReturn(Future.successful(group))
      when(microgridsRepository.retrieveAll(grids)).thenReturn(Future.successful(microgrids))

      whenReady(service.create(group, grids)) { result =>
        result.name must be(group.name)
        (result.grids.map(_.id.get) must contain).allElementsOf(grids)
      }
    }

    "store new group without specified grids that does not exists" in new Fixture {
      val grids: Seq[Long]                      = microgrids.map(_.id.get)
      val withoutFirstMicrogrid: Seq[Microgrid] = microgrids.tail

      when(groupRepository.save(any(classOf[Group]))).thenReturn(Future.successful(group))
      when(microgridsRepository.retrieveAll(grids)).thenReturn(Future.successful(withoutFirstMicrogrid))

      whenReady(service.create(group, grids)) { result =>
        result.name must be(group.name)
        (result.grids.map(_.id.get) must contain)
          .allElementsOf(withoutFirstMicrogrid.map(_.id.get))
      }
    }

    "reject to store group when none of microgrids exist" in new Fixture {
      val grids: Seq[Long] = microgrids.map(_.id.get)

      when(groupRepository.save(group)).thenReturn(Future.successful(group))
      when(microgridsRepository.retrieveAll(grids)).thenReturn(Future.successful(Seq.empty))

      an[IllegalArgumentException] must be thrownBy {
        Await.result(service.create(group, grids), 1.second)
      }
    }

    "retrieve one group" in new Fixture {
      when(groupRepository.retrieveOne(group.id.get)).thenReturn(Future.successful(Option(group)))

      whenReady(service.retrieveOne(group.id.get)) { result =>
        result.name must be(group.name)
      }
    }

    "reject to return one group when it does not exist" in new Fixture {
      when(groupRepository.retrieveOne(group.id.get)).thenReturn(Future.successful(None))

      an[EntityNotFound] must be thrownBy {
        Await.result(service.retrieveOne(group.id.get), 1.second)
      }
    }

    "remove one group" in new Fixture {
      when(groupRepository.remove(group.id.get)).thenReturn(Future.successful(1))

      noException must be thrownBy {
        Await.result(service.remove(group.id.get), 1.second)
      }
    }

    "reject to remove non existing group" in new Fixture {
      when(groupRepository.remove(group.id.get)).thenReturn(Future.successful(0))

      an[EntityNotFound] must be thrownBy {
        Await.result(service.remove(group.id.get), 1.second)
      }
    }
  }

  trait Fixture {
    val groupRepository: GroupRepository          = mock[GroupRepository]
    val microgridsRepository: MicrogridRepository = mock[MicrogridRepository]
    val service                                   = new GroupService(groupRepository, microgridsRepository)

    val microgrids = Seq(
      Microgrid(Option(1), "test-url-1", OSGP, "org-1"),
      Microgrid(Option(2), "test-url-2", MAINFLUX, "org-2"),
      Microgrid(Option(3), "test-url-3", OSGP, "org-3")
    )
    val group = Group(Option(1), "test-name", microgrids)

  }
}
