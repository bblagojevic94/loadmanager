package io.mainflux.loadmanager.postgres

import java.sql.Timestamp
import java.time.LocalDateTime

import io.mainflux.loadmanager.engine.{GroupInfo, Microgrid, PlatformType, SubscriberInfo}
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

trait DatabaseSchema {
  implicit val dtm: JdbcType[LocalDateTime] with BaseTypedType[LocalDateTime] =
    MappedColumnType.base[LocalDateTime, Timestamp](Timestamp.valueOf, _.toLocalDateTime)

  implicit val ptm: JdbcType[PlatformType] with BaseTypedType[PlatformType] =
    MappedColumnType.base[PlatformType, String](_.name, PlatformType.valueOf)

  val microgrids       = TableQuery[Microgrids]
  val groups           = TableQuery[Groups]
  val subscribers      = TableQuery[Subscribers]
  val groupedGrids     = TableQuery[GroupsMicrogrids]
  val subscribedGroups = TableQuery[SubscribersGroups]

  final class Microgrids(tag: Tag) extends Table[Microgrid](tag, "microgrids") {
    def * =
      (id.?, url, platform, organisationId, createdAt) <> (Microgrid.tupled, Microgrid.unapply)

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def url = column[String]("url")

    def platform = column[PlatformType]("platform")

    def organisationId = column[String]("organisation_id")

    def createdAt = column[LocalDateTime]("created_at")
  }

  final class Groups(tag: Tag) extends Table[GroupInfo](tag, "groups") {
    def * = (id.?, name, createdAt) <> (GroupInfo.tupled, GroupInfo.unapply)

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def createdAt = column[LocalDateTime]("created_at")
  }

  final class Subscribers(tag: Tag) extends Table[SubscriberInfo](tag, "subscribers") {
    def * = (id.?, callback, createdAt) <> (SubscriberInfo.tupled, SubscriberInfo.unapply)

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def callback = column[String]("callback")

    def createdAt = column[LocalDateTime]("created_at")
  }

  final class GroupsMicrogrids(tag: Tag) extends Table[(Long, Long)](tag, "groups_microgrids") {
    def * = (groupId, microgridId)

    def groupId = column[Long]("group_id")

    def microgridId = column[Long]("microgrid_id")
  }

  final class SubscribersGroups(tag: Tag) extends Table[(Long, Long)](tag, "subscribers_groups") {
    def * = (subscriberId, groupId)

    def subscriberId = column[Long]("subscriber_id")

    def groupId = column[Long]("group_id")
  }
}
