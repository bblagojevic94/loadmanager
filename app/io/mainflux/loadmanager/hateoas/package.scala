package io.mainflux.loadmanager

import org.joda.time.DateTime

package object hateoas {

  final case class Error(status: String, detail: String)

  final case class ErrorResponse(meta: Meta = Meta(), errors: Seq[Error])

  final case class Meta(createdAt: String = DateTime.now().toString)

  val GroupType     = "groups"
  val MicrogridType = "microgrids"
}
