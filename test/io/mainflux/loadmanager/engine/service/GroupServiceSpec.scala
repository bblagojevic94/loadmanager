package io.mainflux.loadmanager.engine.service

import io.mainflux.loadmanager.engine.model.{Group, Microgrid, Platform}
import io.mainflux.loadmanager.engine.persistence.{GroupRepository, MicrogridRepository}
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
      when(microgridsRepository.findAll(grids)).thenReturn(Future.successful(microgrids))

      whenReady(service.createGroup(group, grids)) { result =>
        result.name must be(group.name)
        (result.grids.map(_.id.get) must contain).allElementsOf(grids)
      }
    }

    "store new group without specified grids that does not exists" in new Fixture {
      val grids: Seq[Long]                      = microgrids.map(_.id.get)
      val withoutFirstMicrogrid: Seq[Microgrid] = microgrids.tail

      when(groupRepository.save(any(classOf[Group]))).thenReturn(Future.successful(group))
      when(microgridsRepository.findAll(grids)).thenReturn(Future.successful(withoutFirstMicrogrid))

      whenReady(service.createGroup(group, grids)) { result =>
        result.name must be(group.name)
        (result.grids.map(_.id.get) must contain)
          .allElementsOf(withoutFirstMicrogrid.map(_.id.get))
      }
    }

    "reject to store group when none of microgrids exist" in new Fixture {
      val grids: Seq[Long] = microgrids.map(_.id.get)

      when(groupRepository.save(group)).thenReturn(Future.successful(group))
      when(microgridsRepository.findAll(grids)).thenReturn(Future.successful(Seq.empty))

      an[IllegalArgumentException] must be thrownBy {
        Await.result(service.createGroup(group, grids), 1.second)
      }
    }
  }

  trait Fixture {
    val groupRepository: GroupRepository          = mock[GroupRepository]
    val microgridsRepository: MicrogridRepository = mock[MicrogridRepository]
    val service                                   = new GroupService(groupRepository, microgridsRepository)

    val microgrids = Seq(
      Microgrid(Option(1), "test-url-1", Platform.OSGP, "org-1"),
      Microgrid(Option(2), "test-url-2", Platform.MAINFLUX, "org-2"),
      Microgrid(Option(3), "test-url-3", Platform.OSGP, "org-3")
    )
    val group = Group(Option(1), "test-name", microgrids)

  }
}
