package io.mainflux.loadmanager.engine

trait Grid {

  def id: Option[Long]

  def aggregateLoad: Double

}
