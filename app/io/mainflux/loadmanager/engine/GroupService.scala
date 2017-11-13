package io.mainflux.loadmanager.engine

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GroupService @Inject()(groupRepository: GroupRepository, microgridRepository: MicrogridRepository)(
    implicit ec: ExecutionContext
) {

  def createGroup(group: Group, grids: Seq[Long]): Future[Group] =
    microgridRepository.retrieveAll(grids).flatMap {
      case Seq()      => Future.failed(new IllegalArgumentException("None of specified microgrids does not exist"))
      case microgrids => groupRepository.save(group.copy(grids = microgrids))
    }

  def retrieveAll(): Future[Seq[Group]] = groupRepository.retrieveAll

  def retrieveOne(id: Long): Future[Group] = groupRepository.retrieveOne(id).flatMap {
    case Some(group) => Future.successful(group)
    case _           => Future.failed(EntityNotFound(s"Group with id $id does not exists"))
  }

  def remove(id: Long): Future[Unit] = groupRepository.remove(id).map {
    case 0 => throw EntityNotFound(s"Group with id $id does not exist")
    case _ =>
  }

}
