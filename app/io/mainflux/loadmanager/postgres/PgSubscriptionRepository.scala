package io.mainflux.loadmanager.postgres

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{Subscription, SubscriptionGroup, SubscriptionRepository}
import io.mainflux.loadmanager.persistence.DatabaseSchema
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class PgSubscriptionRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(
    implicit val ec: ExecutionContext
) extends SubscriptionRepository
    with HasDatabaseConfigProvider[JdbcProfile]
    with DatabaseSchema {

  override def save(subscription: Subscription): Future[Subscription] = {
    val dbAction = (for {
      savedSubscription <- subscriptions
        .returning(subscriptions.map(_.id))
        .into((item, id) => item.copy(id = Some(id))) += subscription

      relations = subscription.groupIds.map { groupId =>
        SubscriptionGroup(savedSubscription.id.get, groupId)
      }

      _ <- subscriptionsGroups.returning(subscriptionsGroups.map(_.groupId)) ++= relations

    } yield savedSubscription).transactionally

    db.run(dbAction)
  }

  override def retrieveOne(id: Long): Future[Option[Subscription]] = {
    val dbAction = for {
      subscription <- subscriptions.filter(_.id === id).result.headOption
      groups       <- subscriptionsGroups.filter(_.subscriptionId === id).result
    } yield subscription.map(a => a.copy(groupIds = groups.map(_.groupId)))

    db.run(dbAction)
  }

  def retrieveAll: Future[Seq[Subscription]] = {
    def fillSubscriptions(subscriptionGroups: Seq[(Subscription, Option[SubscriptionGroup])]) =
      subscriptionGroups.groupBy(_._1).toSeq.map {
        case (subscription, relation) =>
          val groupIds = relation.flatMap(_._2.map(_.groupId))
          subscription.copy(groupIds = groupIds)
      }

    val dbAction = for {
      (subs, subsGroups) <- subscriptions
        .joinLeft(subscriptionsGroups)
        .on(_.id === _.subscriptionId)
    } yield (subs, subsGroups)

    db.run(dbAction.result).map(fillSubscriptions)
  }

  override def remove(id: Long): Future[Int] = {
    val dbAction = (for {
      _       <- subscriptionsGroups.filter(_.subscriptionId === id).delete
      deleted <- subscriptions.filter(_.id === id).delete
    } yield deleted).transactionally

    db.run(dbAction)
  }

  override def subscribeOnGroups(subscriptionId: Long, groupIds: Seq[Long]): Future[Option[Int]] = {
    val dbAction = (for {
      existingRels <- subscriptionsGroups
        .filter(sg => sg.groupId.inSet(groupIds) && sg.subscriptionId === subscriptionId)
        .map(_.groupId)
        .result
      existingGroups <- groups.filter(_.id.inSet(groupIds)).map(_.id).result
      filtered = groupIds.filter(g => !existingRels.contains(g) && existingGroups.contains(g))
      toInsert = filtered.map(groupId => SubscriptionGroup(subscriptionId, groupId))
      count <- subscriptionsGroups ++= toInsert
    } yield count).transactionally

    db.run(dbAction)
  }

  override def unsubscribeFromGroups(subscriptionId: Long, groupIds: Seq[Long]): Future[Int] = {
    val dbAction = subscriptionsGroups
      .filter(sg => sg.subscriptionId === subscriptionId && sg.groupId.inSet(groupIds))
      .delete
    db.run(dbAction)
  }

}
