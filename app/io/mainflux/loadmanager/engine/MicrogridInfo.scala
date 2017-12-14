package io.mainflux.loadmanager.engine

import java.time.LocalDateTime

final case class MicrogridInfo(id: Option[Long],
                               url: String,
                               platform: PlatformType,
                               organisationId: String,
                               createdAt: LocalDateTime)
