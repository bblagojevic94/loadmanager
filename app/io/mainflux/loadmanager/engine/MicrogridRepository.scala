package io.mainflux.loadmanager.engine

import scala.concurrent.Future

trait MicrogridRepository {

  def retrieveAll(grids: Seq[Long]): Future[Seq[Microgrid]]

}
