package io.mainflux.loadmanager.postgres

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{Microgrid, MicrogridRepository}
import io.mainflux.loadmanager.persistence.DatabaseSchema
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

class PgMicrogridRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
    extends MicrogridRepository
    with HasDatabaseConfigProvider[JdbcProfile]
    with DatabaseSchema {

  def findAll(grids: Seq[Long]): Future[Seq[Microgrid]] =
    db.run(microgrids.filter(_.id.inSet(grids)).result)

}
