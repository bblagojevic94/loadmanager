package io.mainflux.loadmanager.postgres

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{SubscriberInfo, SubscriberRepository}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

final class SubscribersDAO @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends SubscriberRepository
    with HasDatabaseConfigProvider[JdbcProfile]
    with DatabaseSchema {

  def save(subscriber: SubscriberInfo): Future[SubscriberInfo] = ???

  def retrieveAll: Future[Seq[SubscriberInfo]] = db.run(subscribers.result)

  def retrieveOne(id: Long): Future[Option[SubscriberInfo]] =
    db.run(subscribers.filter(_.id === id).result.headOption)

  def remove(id: Long): Future[Int] = db.run(subscribers.filter(_.id === id).delete)
}
