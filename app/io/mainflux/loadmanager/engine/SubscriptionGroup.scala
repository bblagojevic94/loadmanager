package io.mainflux.loadmanager.engine

import java.time.LocalDateTime

final case class SubscriptionGroup(subscriptionId: Long,
                                   groupId: Long,
                                   createdAt: LocalDateTime = LocalDateTime.now())
