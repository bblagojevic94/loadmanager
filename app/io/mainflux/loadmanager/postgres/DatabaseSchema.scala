package io.mainflux.loadmanager.postgres

import java.sql.Timestamp
import java.time.LocalDateTime

import io.mainflux.loadmanager.engine.{Group, Microgrid, PlatformType, Subscriber}
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

trait DatabaseSchema {
  implicit val dtm: JdbcType[LocalDateTime] with BaseTypedType[LocalDateTime] =
    MappedColumnType.base[LocalDateTime, Timestamp](Timestamp.valueOf, _.toLocalDateTime)

  implicit val ptm: JdbcType[PlatformType] with BaseTypedType[PlatformType] =
    MappedColumnType.base[PlatformType, String](_.name, PlatformType.valueOf)

  val microgrids: TableQuery[Microgrids]   = TableQuery[Microgrids]
  val groups: TableQuery[Groups]           = TableQuery[Groups]
  val subscribers: TableQuery[Subscribers] = TableQuery[Subscribers]

  final class Microgrids(tag: Tag) extends Table[Microgrid](tag, "microgrids") {
    def * : ProvenShape[Microgrid] =
      (id.?, url, platform, organisationId, createdAt) <> (Microgrid.tupled, Microgrid.unapply)

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def url: Rep[String] = column[String]("url")

    def platform: Rep[PlatformType] = column[PlatformType]("platform")

    def organisationId: Rep[String] = column[String]("organisation_id")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")
  }

  final class Groups(tag: Tag) extends Table[Group](tag, "groups") {
    def * : ProvenShape[Group] = (id.?, name, createdAt) <> (Group.tupled, Group.unapply)

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name: Rep[String] = column[String]("name")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")
  }

  final class Subscribers(tag: Tag) extends Table[Subscriber](tag, "subscribers") {
    def * : ProvenShape[Subscriber] =
      (id.?, callback, createdAt) <> (Subscriber.tupled, Subscriber.unapply)

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def callback: Rep[String] = column[String]("callback")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")
  }
}
