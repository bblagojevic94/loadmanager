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
}
