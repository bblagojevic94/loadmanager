package io.mainflux.loadmanager.postgres

import java.time.LocalDateTime
import javax.inject.Inject

import io.mainflux.loadmanager.engine.{Group, GroupMicrogrid, GroupRepository}
import io.mainflux.loadmanager.persistence.DatabaseSchema
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class PgGroupRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext
) extends GroupRepository
    with HasDatabaseConfigProvider[JdbcProfile]
    with DatabaseSchema {

  def save(group: Group): Future[Group] = {
    val dbAction = (for {
      savedGroup <- groups
        .returning(groups.map(_.id))
        .into((item, id) => item.copy(id = Some(id))) += group

      relations = group.grids.map(
        grid => GroupMicrogrid(savedGroup.id.get, grid.id.get, LocalDateTime.now())
      )

      savedRelations <- groupsMicrogrids
        .returning(groupsMicrogrids.map(_.microgridId)) ++= relations

      microgrids <- microgrids.filter(_.id.inSet(savedRelations)).result
    } yield savedGroup.copy(grids = microgrids)).transactionally

    db.run(dbAction)
  }

}
