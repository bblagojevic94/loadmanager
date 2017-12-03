package io.mainflux.loadmanager.engine

import scala.concurrent.Future

trait SubscriptionRepository {

  def save(subscription: Subscription): Future[Subscription]

  def retrieveOne(id: Long): Future[Option[Subscription]]

  def remove(id: Long): Future[Int]

  def retrieveAll: Future[Seq[Subscription]]

  def subscribeOnGroups(subscriptionId: Long, groupIds: Seq[Long]): Future[Seq[Long]]

  def unsubscribeFromGroups(subscriptionId: Long, groupIds: Seq[Long]): Future[Seq[Long]]

}
