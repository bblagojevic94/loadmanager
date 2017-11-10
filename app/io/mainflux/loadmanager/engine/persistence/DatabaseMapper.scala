package io.mainflux.loadmanager.engine.persistence

import java.sql.Timestamp

import io.mainflux.loadmanager.engine.model.Platform
import org.joda.time.DateTime
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

object DatabaseMapper {
  implicit val dateTimeMapper: JdbcType[DateTime] =
    MappedColumnType.base[DateTime, Timestamp](
      dt => new Timestamp(dt.getMillis),
      ts => new DateTime(ts.getTime)
    )

  implicit val platformMapper: JdbcType[Platform] =
    MappedColumnType.base[Platform, String](_.name, Platform.valueOf)

}
