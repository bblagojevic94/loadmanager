package io.mainflux.loadmanager.engine.persistence

import javax.inject.Inject

import io.mainflux.loadmanager.engine.model.Group
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

trait GroupRepository {

  def save(group: Group): Future[Group]

}

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

      groupMicrogridsEntries = group.grids.map(grid => GroupMicrogrid(savedGroup.id.get, grid.id.get))

      relationships <- groupsMicrogrids
        .returning(groupsMicrogrids.map(_.microgridId)) ++= groupMicrogridsEntries

      microgrids <- microgrids.filter(_.id.inSet(relationships)).result
    } yield savedGroup.copy(grids = microgrids)).transactionally

    db.run(dbAction)
  }

}
