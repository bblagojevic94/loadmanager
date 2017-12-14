package io.mainflux.loadmanager.postgres

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{Group, GroupRepository}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

final class GroupsDAO @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends GroupRepository
    with HasDatabaseConfigProvider[JdbcProfile]
    with DatabaseSchema {

  def save(group: Group): Future[Long] =
    db.run(groups.returning(groups.map(_.id)) += group)

  def retrieveAll: Future[Seq[Group]] = db.run(groups.result)

  def retrieveOne(id: Long): Future[Option[Group]] =
    db.run(groups.filter(_.id === id).result.headOption)

  def remove(id: Long): Future[Int] = db.run(groups.filter(_.id === id).delete)
}
