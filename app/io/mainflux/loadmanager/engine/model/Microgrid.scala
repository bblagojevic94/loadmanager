package io.mainflux.loadmanager.engine.model

case class Microgrid(id: Option[Long] = None, url: String, platform: Platform, organisationId: String)
    extends Grid {

  // TODO: implement calculating aggregate load for single microgrid
  override def calculateAggregateLoad: Double = ???
}
