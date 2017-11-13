package io.mainflux.loadmanager.engine

import scala.concurrent.Future

trait MicrogridRepository {

  def findAll(grids: Seq[Long]): Future[Seq[Microgrid]]

}
