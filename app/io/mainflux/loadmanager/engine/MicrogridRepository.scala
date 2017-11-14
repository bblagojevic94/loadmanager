package io.mainflux.loadmanager.engine

import scala.concurrent.Future

trait MicrogridRepository {

  def save(microgrid: Microgrid): Future[Microgrid]

  def retrieveOne(id: Long): Future[Option[Microgrid]]

  def retrieveAll: Future[Seq[Microgrid]]

  def retrieveAll(grids: Seq[Long]): Future[Seq[Microgrid]]

}
