package io.mainflux.loadmanager.hateoas

final case class MicrogridIdentifier(`type`: String, id: Long)

final case class MicrogridIdentifiers(data: Seq[MicrogridIdentifier])
