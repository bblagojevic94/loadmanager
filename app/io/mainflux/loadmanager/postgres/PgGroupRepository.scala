package io.mainflux.loadmanager.postgres

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

  override def save(group: Group): Future[Group] = {
    val dbAction = (for {
      savedGroup <- groups
        .returning(groups.map(_.id))
        .into((item, id) => item.copy(id = Some(id))) += group

      relations = group.grids.map { grid =>
        GroupMicrogrid(savedGroup.id.get, grid.id.get)
      }

      savedRelations <- groupsMicrogrids.returning(groupsMicrogrids.map(_.microgridId)) ++= relations

      microgrids <- microgrids.filter(_.id.inSet(savedRelations)).result
    } yield savedGroup.copy(grids = microgrids)).transactionally

    db.run(dbAction)
  }

  override def retrieveAll(groupIds: Set[Long] = Set()): Future[Seq[Group]] = {
    def fillGroups(groupMicrogrids: Seq[(Group, Option[(GroupMicrogrid, Microgrid)])]) =
      groupMicrogrids.groupBy(_._1).toSeq.map {
        case (group, relation) =>
          val grids = relation.flatMap(_._2.map(_._2))
          group.copy(grids = grids)
      }

    val query = if (groupIds.isEmpty) groups else groups.filter(_.id.inSet(groupIds))
    val dbAction = for {
      (groups, groupMicrogrids) <- query
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
      _       <- subscriptionsGroups.filter(_.groupId === id).delete
      deleted <- groups.filter(_.id === id).delete
    } yield deleted).transactionally

    db.run(dbAction)
  }

  override def addMicrogrids(groupId: Long, microgridIds: Seq[Long]): Future[Seq[Microgrid]] = {
    val dbAction = (for {
      existingRels <- groupsMicrogrids
        .filter(mg => mg.microgridId.inSet(microgridIds) && mg.groupId === groupId)
        .map(_.microgridId)
        .result
      existingMgs <- microgrids.filter(_.id.inSet(microgridIds)).result
      filtered = microgridIds.filter { mg =>
        !existingRels.contains(mg) && existingMgs.map(_.id.get).contains(mg)
      }
      toInsert = filtered.map(mgId => GroupMicrogrid(groupId, mgId))
      _ <- groupsMicrogrids ++= toInsert
    } yield existingMgs).transactionally

    db.run(dbAction)
  }

  override def removeMicrogrids(groupId: Long, microgrids: Seq[Long]): Future[Seq[Long]] = {
    val query = groupsMicrogrids.filter(mg => mg.groupId === groupId && mg.microgridId.inSet(microgrids))
    val dbAction = (for {
      toRemove <- query.result
      _        <- query.delete
    } yield toRemove.map(_.microgridId)).transactionally
    db.run(dbAction)
  }

  override def retrieveAllBySubscription(subscriptionId: Long): Future[Seq[Long]] = {
    val dbAction = subscriptionsGroups.filter(_.subscriptionId === subscriptionId).result
    db.run(dbAction).map(_.map(_.groupId))
  }

  override def hasSubscriptions(groupId: Long): Future[Boolean] = {
    val dbAction = subscriptionsGroups.filter(_.groupId === groupId).exists
    db.run(dbAction.result)
  }
}
