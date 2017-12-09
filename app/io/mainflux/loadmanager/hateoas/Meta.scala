package io.mainflux.loadmanager.hateoas

import org.joda.time.DateTime

final case class Meta(createdAt: String = DateTime.now().toString)
