package io.mainflux.loadmanager.controllers

import java.time.LocalDateTime

import io.mainflux.loadmanager.hateoas.{Error, ErrorResponse, JsonFormat, Meta}
import play.api.http.HttpErrorHandler
import play.api.libs.json.{JsPath, JsValue, Json, JsonValidationError}
import play.api.mvc.{RequestHeader, _}

import scala.concurrent.Future

abstract class ApiEndpoint(cc: ControllerComponents, errorHandler: HttpErrorHandler)
    extends AbstractController(cc)
    with JsonFormat
    with BodyParserUtils {

  private type JsErrors = Seq[(JsPath, Seq[JsonValidationError])]

  protected def createErrorResponse(errors: JsErrors): Future[Nothing] =
    Future.failed(new IllegalArgumentException(s"Malformed JSON provided."))

  private def createBadResult(msg: String) = { request: RequestHeader =>
    errorHandler.onServerError(request, new IllegalArgumentException(msg))
  }

  protected def parseJsonAPI: BodyParser[JsValue] = when(
    _.contentType.exists(m => m.equals(ContentType)),
    cc.parsers.tolerantJson,
    createBadResult(s"Expecting $ContentType body")
  )

  val NotSupportedResponse: JsValue = {
    val er = ErrorResponse(
      meta = Meta(LocalDateTime.now().toString),
      errors = Seq(Error("403", "Server does not support full replacement of a to-many relationship."))
    )

    Json.toJson(er)
  }
}
