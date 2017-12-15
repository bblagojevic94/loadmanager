package io.mainflux.loadmanager.engine

import java.time.LocalDateTime

final case class SubscriberInfo(id: Option[Long], callback: String, createdAt: LocalDateTime)
