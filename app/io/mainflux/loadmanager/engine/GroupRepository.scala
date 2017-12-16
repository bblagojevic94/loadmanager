package io.mainflux.loadmanager.engine

import scala.concurrent.Future

trait GroupRepository extends Repository[Group] {
  def addMicrogrids(groupId: Long, microgrids: Set[Long]): Future[Option[Int]]

  def removeMicrogrids(groupId: Long, microgrids: Set[Long]): Future[Int]
}
