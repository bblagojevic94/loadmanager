package io.mainflux.loadmanager.engine

import java.time.LocalDateTime

final case class Microgrid(id: Option[Long],
                           url: String,
                           platform: Platform,
                           organisationId: String,
                           createdAt: LocalDateTime)
