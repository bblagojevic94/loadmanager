package io.mainflux.loadmanager.engine

import java.time.LocalDateTime

final case class GroupInfo(id: Option[Long], name: String, createdAt: LocalDateTime)
