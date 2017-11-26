package io.mainflux.loadmanager.engine

import java.time.LocalDateTime

final case class Subscription(id: Option[Long] = None,
                              callback: String,
                              createdAt: LocalDateTime = LocalDateTime.now(),
                              groupIds: Seq[Long] = Seq())
