package io.mainflux.loadmanager.osgp

import akka.actor.{Actor, ActorLogging}
import io.mainflux.loadmanager.engine.Grid
import io.mainflux.loadmanager.engine.Subscriptions.AggregateLoad

import scala.util.Random

object Worker {

  final case class CalculateLoad(groupId: Long, grid: Grid)
}

class Worker extends Actor with ActorLogging {
  import Worker._

  override def receive: Receive = {
    case CalculateLoad(groupId, grid) =>
      // TODO: create real request
      val load = Random.nextDouble()
      sender() ! AggregateLoad(groupId, grid, load)
  }
}
