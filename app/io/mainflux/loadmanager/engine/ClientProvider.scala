package io.mainflux.loadmanager.engine

class ClientProvider {
  def clientFor(platform: Platform): PlatformClient = platform.client()
}
