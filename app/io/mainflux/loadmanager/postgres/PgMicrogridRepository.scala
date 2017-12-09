package io.mainflux.loadmanager.postgres

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{Microgrid, MicrogridRepository}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class PgMicrogridRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(
    implicit val ec: ExecutionContext
) extends MicrogridRepository
    with HasDatabaseConfigProvider[JdbcProfile]
    with DatabaseSchema {

  override def save(microgrid: Microgrid): Future[Microgrid] = {
    val dbAction = microgrids
      .returning(microgrids.map(_.id))
      .into((item, id) => item.copy(id = Some(id))) += microgrid
    db.run(dbAction)
  }

  override def retrieveOne(id: Long): Future[Option[Microgrid]] =
    db.run(microgrids.filter(_.id === id).result.headOption)

  override def retrieveAll: Future[Seq[Microgrid]] =
    db.run(microgrids.result)

  override def retrieveAllByIds(grids: Seq[Long]): Future[Seq[Microgrid]] =
    db.run(microgrids.filter(_.id.inSet(grids)).result)

  override def retrieveAllByGroup(groupId: Long): Future[Seq[Long]] = {
    val dbAction = groupsMicrogrids.filter(_.groupId === groupId).result
    db.run(dbAction).map(_.map(_.microgridId))
  }

}
