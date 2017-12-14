package io.mainflux.loadmanager.hateoas

import play.api.libs.json.{Json, OFormat}

trait JsonFormat {
  implicit val meta: OFormat[Meta]              = Json.format[Meta]
  implicit val error: OFormat[Error]            = Json.format[Error]
  implicit val errorRes: OFormat[ErrorResponse] = Json.format[ErrorResponse]

  val ContentType: String = "application/vnd.api+json"
}
