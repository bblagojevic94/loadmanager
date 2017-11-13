package io.mainflux.loadmanager.engine

import java.time.LocalDateTime

final case class Group(id: Option[Long] = None,
                 name: String,
                 grids: Seq[Grid] = Seq[Grid](),
                 createdAt: LocalDateTime = LocalDateTime.now())
    extends Grid {

  // TODO: implement calculating aggregate load for one Group
  override def aggregateLoad: Double = ???
}
