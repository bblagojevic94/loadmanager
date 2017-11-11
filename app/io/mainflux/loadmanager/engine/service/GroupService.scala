package io.mainflux.loadmanager.engine.service

import javax.inject.Inject

import io.mainflux.loadmanager.engine.model.Group
import io.mainflux.loadmanager.engine.persistence.{GroupRepository, MicrogridRepository}

import scala.concurrent.{ExecutionContext, Future}

class GroupService @Inject()(groupRepository: GroupRepository, microgridRepository: MicrogridRepository)(
    implicit ec: ExecutionContext
) {

  def createGroup(group: Group, grids: Seq[Long]): Future[Group] =
    microgridRepository.findAll(grids).flatMap {
      case Seq()      => Future.failed(new IllegalArgumentException("None of specified microgrids does not exist"))
      case microgrids => groupRepository.save(group.copy(grids = microgrids))
    }
}
