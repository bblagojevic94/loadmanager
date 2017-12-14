package io.mainflux.loadmanager.engine

import scala.concurrent.Future

trait MicrogridRepository {
  def save(microgrid: MicrogridInfo): Future[Long]

  def retrieveAll: Future[Seq[MicrogridInfo]]

  def retrieveOne(id: Long): Future[Option[MicrogridInfo]]
}
