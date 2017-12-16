package io.mainflux.loadmanager.engine

import scala.concurrent.Future

trait SubscriberRepository extends Repository[Subscriber] {
  def subscribe(subscriberId: Long, groups: Set[Long]): Future[Option[Int]]

  def unsubscribe(subscriberId: Long, groups: Set[Long]): Future[Int]
}
