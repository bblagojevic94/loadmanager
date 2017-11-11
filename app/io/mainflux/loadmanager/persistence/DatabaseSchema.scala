package io.mainflux.loadmanager.persistence

import java.time.LocalDateTime

import io.mainflux.loadmanager.engine.{Group, Microgrid, Platform}
import io.mainflux.loadmanager.persistence.DatabaseMapper._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, TableQuery}

trait DatabaseSchema {

  val microgrids: TableQuery[MicrogridRow]            = TableQuery[MicrogridRow]
  val groups: TableQuery[GroupRow]                    = TableQuery[GroupRow]
  val groupsMicrogrids: TableQuery[GroupMicrogridRow] = TableQuery[GroupMicrogridRow]

  class MicrogridRow(tag: Tag) extends Table[Microgrid](tag, "microgrids") {
    def * : ProvenShape[Microgrid] = {

      val props = (id.?, url, platform, organisationId, createdAt)

      props <> (Microgrid.tupled, Microgrid.unapply)
    }

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def url: Rep[String] = column[String]("url")

    def platform: Rep[Platform] = column[Platform]("platform")

    def organisationId: Rep[String] = column[String]("organisation_id")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

  }

  class GroupRow(tag: Tag) extends Table[Group](tag, "groups") {
    def * : ProvenShape[Group] = {

      val props = (id.?, name, createdAt).shaped

      props.<>({ tuple =>
        Group.apply(id = tuple._1, name = tuple._2, createdAt = tuple._3)
      }, { (group: Group) =>
        Some((group.id, group.name, group.createdAt))
      })
    }

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name: Rep[String] = column[String]("name")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

  }

  class GroupMicrogridRow(tag: Tag) extends Table[GroupMicrogrid](tag, "groups_microgrids") {
    def * : ProvenShape[GroupMicrogrid] = {

      val props = (groupId, microgridId, createdAt)

      props <> (GroupMicrogrid.tupled, GroupMicrogrid.unapply)
    }

    def groupId: Rep[Long] = column[Long]("group_id")

    def microgridId: Rep[Long] = column[Long]("microgrid_id")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

  }

  case class GroupMicrogrid(groupId: Long, microgridId: Long, createdAt: LocalDateTime)

}
