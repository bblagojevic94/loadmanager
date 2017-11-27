package io.mainflux.loadmanager.engine

import scala.concurrent.Future

trait GroupRepository {

  def save(group: Group): Future[Group]

  def retrieveAll: Future[Seq[Group]]

  def retrieveAllByIds(groupIds: Seq[Long]): Future[Seq[Group]]

  def retrieveOne(id: Long): Future[Option[Group]]

  def remove(id: Long): Future[Int]

  def addMicrogrids(groupId: Long, microgrids: Seq[Long]): Future[Option[Int]]

  def removeMicrogrids(groupId: Long, microgrids: Seq[Long]): Future[Int]

  def retrieveAllBySubscription(subscriptionId: Long): Future[Seq[Long]]

}
