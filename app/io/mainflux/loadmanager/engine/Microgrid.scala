package io.mainflux.loadmanager.engine

import java.time.LocalDateTime

case class Microgrid(id: Option[Long] = None,
                     url: String,
                     platform: Platform,
                     organisationId: String,
                     createdAt: LocalDateTime = LocalDateTime.now())
    extends Grid {

  // TODO: implement calculating aggregate load for single microgrid
  override def aggregateLoad: Double = ???
}
