package io.mainflux.loadmanager.engine

import java.time.LocalDateTime

final case class GroupMicrogrid(groupId: Long, microgridId: Long, createdAt: LocalDateTime)
