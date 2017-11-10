package io.mainflux.loadmanager.engine.model

trait Grid {

  def id: Option[Long]

  def calculateAggregateLoad: Double
}
