package io.mainflux.loadmanager.engine.persistence

import javax.inject.Inject

import io.mainflux.loadmanager.engine.model.Microgrid
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

trait MicrogridRepository {

  def findAll(grids: Seq[Long]): Future[Seq[Microgrid]]

}

class PgMicrogridRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
    extends MicrogridRepository
    with HasDatabaseConfigProvider[JdbcProfile]
    with DatabaseSchema {

  def findAll(grids: Seq[Long]): Future[Seq[Microgrid]] =
    db.run(microgrids.filter(_.id.inSet(grids)).result)

}
