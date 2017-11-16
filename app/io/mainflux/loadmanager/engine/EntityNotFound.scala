package io.mainflux.loadmanager.engine

final case class EntityNotFound(message: String) extends RuntimeException(message)
