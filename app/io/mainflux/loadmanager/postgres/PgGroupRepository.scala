package io.mainflux.loadmanager.postgres

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{Group, GroupInfo, GroupRepository, Microgrid}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

final class PgGroupRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends GroupRepository
    with HasDatabaseConfigProvider[JdbcProfile]
    with DatabaseSchema
    with PgErrorHandler {

  def save(group: Group): Future[Group] = {
    val groupRepo = groups.returning(groups.map(_.id)).into((g, id) => g.copy(id = Some(id)))

    val actions = for {
      sg <- groupRepo += group.info
      pairs = group.microgrids.map(id => (sg.id.getOrElse(0L), id)).toSeq
      _ <- groupedGrids ++= pairs
    } yield group.copy(info = sg)

    db.run(actions.transactionally)
      .recoverWith(handlePgErrors(Microgrid.toString))
  }

  def retrieveAll: Future[Seq[Group]] = {
    val action = for {
      g  <- groups
      gs <- groupedGrids if g.id === gs.groupId
    } yield (g, gs.microgridId)

    db.run(action.result).map(buildGroups)
  }

  def retrieveOne(id: Long): Future[Option[Group]] = {
    val action = for {
      g  <- groups.filter(_.id === id)
      gs <- groupedGrids
    } yield (g, gs.microgridId)

    db.run(action.result).map(buildGroups).map(_.headOption)
  }

  private def buildGroups(rs: Seq[(GroupInfo, Long)]) =
    rs.groupBy(_._1)
      .map {
        case (info, vals) => Group(info, vals.map(_._2).toSet)
      }
      .toSeq

  def remove(id: Long): Future[Int] = db.run(groups.filter(_.id === id).delete)

  def addMicrogrids(groupId: Long, microgrids: Set[Long]): Future[Option[Int]] = {
    val toInsert = microgrids.map(id => (groupId, id))

    db.run(groupedGrids ++= toInsert)
      .recoverWith(handlePgErrors(Microgrid.toString))
  }

  def removeMicrogrids(groupId: Long, microgrids: Set[Long]): Future[Int] = {
    val query =
      groupedGrids.filter(gg => gg.groupId === groupId && gg.microgridId.inSet(microgrids))

    db.run(query.delete)
  }

}
