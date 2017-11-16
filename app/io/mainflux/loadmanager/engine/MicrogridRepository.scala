package io.mainflux.loadmanager.engine

import scala.concurrent.Future

trait MicrogridRepository {

  def save(microgrid: Microgrid): Future[Microgrid]

  def retrieveOne(id: Long): Future[Option[Microgrid]]

  def retrieveAll: Future[Seq[Microgrid]]

  def retrieveAllByIds(grids: Seq[Long]): Future[Seq[Microgrid]]

  def retrieveAllByGroup(groupId: Long): Future[Seq[Long]]

}
