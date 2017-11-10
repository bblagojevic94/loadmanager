package io.mainflux.loadmanager.engine.persistence

import io.mainflux.loadmanager.engine.model.{Group, Microgrid, Platform}
import io.mainflux.loadmanager.engine.persistence.DatabaseMapper._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, TableQuery}

trait DatabaseSchema {

  val microgrids: TableQuery[Microgrids]             = TableQuery[Microgrids]
  val groups: TableQuery[Groups]                     = TableQuery[Groups]
  val groupsMicrogrids: TableQuery[GroupsMicrogrids] = TableQuery[GroupsMicrogrids]

  class Microgrids(tag: Tag) extends Table[Microgrid](tag, "microgrids") {
    def * : ProvenShape[Microgrid] = {

      val props = (id.?, url, platform, organisationId)

      props <> (Microgrid.tupled, Microgrid.unapply)
    }

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def url: Rep[String] = column[String]("url")

    def platform: Rep[Platform] = column[Platform]("platform")

    def organisationId: Rep[String] = column[String]("organisation_id")

  }

  class Groups(tag: Tag) extends Table[Group](tag, "groups") {
    def * : ProvenShape[Group] = {

      val props = (id.?, name).shaped

      props.<>({ tuple =>
        Group.apply(id = tuple._1, name = tuple._2)
      }, { (group: Group) =>
        Some((group.id, group.name))
      })
    }

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name: Rep[String] = column[String]("name")

  }

  class GroupsMicrogrids(tag: Tag) extends Table[GroupMicrogrid](tag, "groups_microgrids") {
    def * : ProvenShape[GroupMicrogrid] = {

      val props = (groupId, microgridId)

      props <> (GroupMicrogrid.tupled, GroupMicrogrid.unapply)
    }

    def groupId: Rep[Long] = column[Long]("group_id")

    def microgridId: Rep[Long] = column[Long]("microgrid_id")

  }

  case class GroupMicrogrid(groupId: Long, microgridId: Long)

}
