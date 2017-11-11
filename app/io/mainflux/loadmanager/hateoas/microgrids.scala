package io.mainflux.loadmanager.hateoas

case class MicrogridIdentifier(`type`: String, id: Long)

case class MicrogridIdentifiers(data: Seq[MicrogridIdentifier])
