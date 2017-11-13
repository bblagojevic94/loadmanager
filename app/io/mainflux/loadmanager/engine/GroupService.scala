package io.mainflux.loadmanager.engine

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GroupService @Inject()(groupRepository: GroupRepository, microgridRepository: MicrogridRepository)(
    implicit ec: ExecutionContext
) {

  def createGroup(group: Group, grids: Seq[Long]): Future[Group] =
    microgridRepository.findAll(grids).flatMap {
      case Seq()      => Future.failed(new IllegalArgumentException("None of specified microgrids does not exist"))
      case microgrids => groupRepository.save(group.copy(grids = microgrids))
    }
}
