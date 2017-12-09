package io.mainflux.loadmanager.postgres

import java.sql.Timestamp
import java.time.LocalDateTime

import io.mainflux.loadmanager.engine.Platform
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

object DatabaseMapper {
  implicit val dateTimeMapper: JdbcType[LocalDateTime] with BaseTypedType[LocalDateTime] =
    MappedColumnType.base[LocalDateTime, Timestamp](
      dt => Timestamp.valueOf(dt),
      ts => ts.toLocalDateTime
    )

  implicit val platformMapper: JdbcType[Platform] =
    MappedColumnType.base[Platform, String](_.name, Platform.valueOf)

}
