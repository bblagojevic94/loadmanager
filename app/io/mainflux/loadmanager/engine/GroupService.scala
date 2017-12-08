package io.mainflux.loadmanager.engine

import javax.inject.Named

import akka.actor.ActorRef
import com.google.inject.Inject
import io.mainflux.loadmanager.engine.Subscriptions.{AddMicrogrid, RemoveGroup, RemoveMicrogrid}

import scala.concurrent.{ExecutionContext, Future}

class GroupService @Inject()(groupRepository: GroupRepository,
                             microgridRepository: MicrogridRepository,
                             @Named("subscription") subscription: ActorRef)(implicit ec: ExecutionContext) {

  def create(group: Group, gridIds: Seq[Long]): Future[Group] =
    microgridRepository
      .retrieveAllByIds(gridIds)
      .flatMap {
        case Seq() =>
          Future.failed(new IllegalArgumentException("None of specified microgrids does not exist"))
        case microgrids => groupRepository.save(group.copy(grids = microgrids))
      }

  def retrieveAll: Future[Seq[Group]] = groupRepository.retrieveAll()

  def retrieveOne(id: Long): Future[Group] =
    groupRepository
      .retrieveOne(id)
      .map(_.getOrElse(throw EntityNotFound(s"Group with id $id does not exists")))

  def remove(id: Long): Future[Unit] =
    groupRepository
      .remove(id)
      .map {
        case 0 => throw EntityNotFound(s"Group with id $id does not exist")
        case _ => subscription ! RemoveGroup(id)
      }

  def retrieveGroupMicrogrids(groupId: Long): Future[Seq[Long]] =
    groupRepository.retrieveOne(groupId).flatMap {
      case Some(_) => microgridRepository.retrieveAllByGroup(groupId)
      case _       => Future.failed(EntityNotFound(s"Group with id $groupId does not exists"))
    }

  def addMicrogrids(groupId: Long, microgridIds: Seq[Long]): Future[Unit] =
    groupRepository.retrieveOne(groupId).flatMap {
      case Some(_) =>
        val fResult = for {
          grids      <- groupRepository.addMicrogrids(groupId, microgridIds)
          subscribed <- groupRepository.hasSubscriptions(groupId)
        } yield (grids, subscribed)

        fResult.map {
          case (addedMicrogrids, hasSubscriptions) if hasSubscriptions =>
            addedMicrogrids.foreach { mg =>
              subscription ! AddMicrogrid(mg, groupId)
            }
        }
      case _ => Future.failed(EntityNotFound(s"Group with id $groupId does not exists"))
    }

  def removeMicrogrids(groupId: Long, microgridIds: Seq[Long]): Future[Unit] =
    groupRepository.retrieveOne(groupId).flatMap {
      case Some(_) =>
        val fResult = for {
          removedGrids <- groupRepository.removeMicrogrids(groupId, microgridIds)
          subscribed   <- groupRepository.hasSubscriptions(groupId)
        } yield (removedGrids, subscribed)

        fResult.map {
          case (removedGrids, hasSubscriptions) if hasSubscriptions =>
            removedGrids.foreach { mgId =>
              subscription ! RemoveMicrogrid(mgId, groupId)
            }
        }
      case _ => Future.failed(EntityNotFound(s"Group with id $groupId does not exists"))
    }
}
