package io.mainflux.loadmanager.postgres

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{Subscriber, SubscriberInfo, SubscriberRepository}
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

  def save(subscriber: Subscriber): Future[Subscriber] = {
    val subscriberRepo =
      subscribers.returning(subscribers.map(_.id)).into((sub, id) => sub.copy(id = Some(id)))

    val actions = for {
      ss <- subscriberRepo += subscriber.info
      pairs = subscriber.groups.map(id => (ss.id.getOrElse(0L), id)).toSeq
      _ <- subscribedGroups ++= pairs
    } yield subscriber.copy(info = ss)

    db.run(actions.transactionally)
  }

  def retrieveAll: Future[Seq[Subscriber]] = {
    val action = for {
      s  <- subscribers
      ss <- subscribedGroups if s.id === ss.subscriberId
    } yield (s, ss.groupId)

    db.run(action.result).map(buildSubscribers)
  }

  def retrieveOne(id: Long): Future[Option[Subscriber]] = {
    val action = for {
      s  <- subscribers
      ss <- subscribedGroups if s.id === ss.subscriberId
    } yield (s, ss.groupId)

    db.run(action.result).map(buildSubscribers).map(_.headOption)
  }

  private def buildSubscribers(rs: Seq[(SubscriberInfo, Long)]) =
    rs.groupBy(_._1)
      .map {
        case (info, vals) => Subscriber(info, vals.map(_._2).toSet)
      }
      .toSeq

  def remove(id: Long): Future[Int] = db.run(subscribers.filter(_.id === id).delete)
}
