package io.mainflux.loadmanager.controllers

import play.api.http.LazyHttpErrorHandler
import play.api.http.Status.{BAD_REQUEST, UNSUPPORTED_MEDIA_TYPE}
import play.api.libs.json.JsValue
import play.api.mvc.{BodyParser, BodyParsers, RequestHeader, Result}

import scala.concurrent.Future

object JsonApiParser extends BodyParsers {

  val JsonAPIContentType: String = "application/vnd.api+json"

  private def createBadResult(msg: String, statusCode: Int = BAD_REQUEST): RequestHeader => Future[Result] = {
    request =>
      LazyHttpErrorHandler.onClientError(request, statusCode, msg)
  }

  def json: BodyParser[JsValue] =
    parse.when(
      _.contentType.exists(m => m.equals(JsonAPIContentType)),
      parse.tolerantJson(parse.DefaultMaxTextLength),
      createBadResult(s"Expecting $JsonAPIContentType", UNSUPPORTED_MEDIA_TYPE)
    )
}
