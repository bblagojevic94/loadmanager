package io.mainflux.loadmanager.engine.model

case class Group(id: Option[Long] = None, name: String, grids: Seq[Grid] = Seq[Grid]()) extends Grid {

  // TODO: implement calculating aggreagate load for one Group
  override def calculateAggregateLoad: Double = ???
}
