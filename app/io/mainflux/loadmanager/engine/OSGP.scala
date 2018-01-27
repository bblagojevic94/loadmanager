package io.mainflux.loadmanager.engine

import scala.concurrent.Future

final class OSGP extends PlatformClient {
  override def loadOf(microgrid: Microgrid): Future[Double] = Future.successful(0)
}
