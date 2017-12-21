package io.mainflux.loadmanager.postgres

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{Microgrid, MicrogridRepository}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

final class PgMicrogridRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
    extends MicrogridRepository
    with HasDatabaseConfigProvider[JdbcProfile]
    with DatabaseSchema {

  def save(microgrid: Microgrid): Future[Microgrid] = {
    val mgRepo = microgrids.returning(microgrids.map(_.id)).into((mg, id) => mg.copy(id = Some(id)))
    db.run(mgRepo += microgrid)
  }

  def retrieveAll: Future[Seq[Microgrid]] = db.run(microgrids.result)

  def retrieveOne(id: Long): Future[Option[Microgrid]] =
    db.run(microgrids.filter(_.id === id).result.headOption)

  def remove(id: Long): Future[Int] = db.run(microgrids.filter(_.id === id).delete)
}
