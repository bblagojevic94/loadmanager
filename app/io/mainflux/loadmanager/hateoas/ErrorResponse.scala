package io.mainflux.loadmanager.hateoas

final case class ErrorResponse(meta: Meta, errors: Seq[Error])
