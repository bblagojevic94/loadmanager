package io.mainflux.loadmanager.engine

import scala.concurrent.Future

trait GroupRepository {

  def save(group: Group): Future[Group]

  def retrieveAll: Future[Seq[Group]]

  def retrieveOne(id: Long): Future[Option[Group]]

  def remove(id: Long): Future[Int]

}
