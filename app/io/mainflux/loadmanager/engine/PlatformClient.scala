package io.mainflux.loadmanager.engine

import scala.concurrent.Future

trait PlatformClient {
  def loadOf(microgrid: Microgrid): Future[Double]
}
