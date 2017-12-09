package io.mainflux.loadmanager.hateoas

final case class ErrorResponse(meta: Meta = Meta(), errors: Seq[Error])
