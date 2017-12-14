package io.mainflux.loadmanager.engine

import scala.concurrent.Future

trait GroupRepository {
  def save(group: GroupInfo): Future[Long]

  def retrieveAll: Future[Seq[GroupInfo]]

  def retrieveOne(id: Long): Future[Option[GroupInfo]]

  def remove(id: Long): Future[Int]
}
