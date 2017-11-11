package io.mainflux.loadmanager.hateoas

case class MicroGridIdentifier(`type`: String, id: Long)

case class MicroGridIdentifierCollection(data: Seq[MicroGridIdentifier])
