package io.mainflux.loadmanager

import org.joda.time.DateTime

package object hateoas {

  case class Error(status: String, detail: String)

  case class ErrorResponse(meta: Meta = Meta(), errors: Seq[Error])

  case class Meta(createdAt: String = DateTime.now().toString)

  val GroupType     = "groups"
  val MicroGridType = "microgrids"
}
