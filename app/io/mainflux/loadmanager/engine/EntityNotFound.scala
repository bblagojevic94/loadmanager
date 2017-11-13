package io.mainflux.loadmanager.engine

case class EntityNotFound(message: String) extends RuntimeException(message)
