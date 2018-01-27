package io.mainflux.loadmanager.engine

class ClientProvider {
  def clientFor(platformType: Platform): PlatformClient =
    platformType match {
      case Platform.OSGP     => new OSGP()
      case Platform.MAINFLUX => new Mainflux()
    }
}
