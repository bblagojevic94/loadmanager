package io.mainflux.loadmanager.postgres

import java.time.LocalDateTime
import javax.inject.Inject

import io.mainflux.loadmanager.engine.{Group, GroupMicrogrid, GroupRepository, Microgrid}
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

  override def retrieveAll: Future[Seq[Group]] = {
    def fillGroups(groupMicrogrids: Seq[(Group, Option[(GroupMicrogrid, Microgrid)])]) =
      groupMicrogrids.groupBy(_._1).toSeq.map {
        case (group, relation) =>
          val grids = relation.flatMap(_._2.map(_._2))
          group.copy(grids = grids)
      }

    val dbAction = for {
      (groups, groupMicrogrids) <- groups
        .joinLeft(groupsMicrogrids.join(microgrids).on(_.microgridId === _.id))
        .on(_.id === _._1.groupId)
    } yield (groups, groupMicrogrids)

    db.run(dbAction.result).map(fillGroups)
  }

  override def retrieveOne(id: Long): Future[Option[Group]] = {
    val dbAction = for {
      relations <- groupsMicrogrids.filter(_.groupId === id).result
      grids     <- microgrids.filter(_.id.inSet(relations.map(_.microgridId))).result
      group     <- groups.filter(_.id === id).result.headOption
    } yield group.map(_.copy(grids = grids))

    db.run(dbAction)
  }

  override def remove(id: Long): Future[Int] = {
    val dbAction = (for {
      _       <- groupsMicrogrids.filter(_.groupId === id).delete
      deleted <- groups.filter(_.id === id).delete
    } yield deleted).transactionally

    db.run(dbAction)
  }
}
