package io.mainflux.loadmanager.engine

import java.time.LocalDateTime

final case class Subscriber(id: Option[Long], callback: String, cretedAt: LocalDateTime)
