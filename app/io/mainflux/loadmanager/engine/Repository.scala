package io.mainflux.loadmanager.engine

import scala.concurrent.Future

trait Repository[A] {
  def save(item: A): Future[A]

  def retrieveAll: Future[Seq[A]]

  def retrieveOne(id: Long): Future[Option[A]]

  def remove(id: Long): Future[Int]
}
