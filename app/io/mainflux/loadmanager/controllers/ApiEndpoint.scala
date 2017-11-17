package io.mainflux.loadmanager.controllers

import io.mainflux.loadmanager.hateoas.{Error, ErrorResponse}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsValue, Json}
import play.api.mvc.Controller

import scala.concurrent.Future
import scala.util.Try

trait ApiEndpoint extends Controller {
  protected def createErrorResponse(errors: Seq[(JsPath, Seq[ValidationError])]): Future[Nothing] = {
    val invalidProperty: Option[String] = Try(errors.head._1.path.last.toString.tail).toOption
    val detail: String = invalidProperty match {
      case Some(property) =>
        s"Property $property caused ${errors.head._2.head.messages.mkString(";")}"
      case _ => s"Reason: ${errors.head._2.head.messages.mkString(";")}"
    }
    Future.failed(new IllegalArgumentException(s"Malformed JSON provided. $detail"))
  }

  val NotSupportedResponse: JsValue = {
    import io.mainflux.loadmanager.hateoas.JsonFormat._

    val response = ErrorResponse(
      errors = Seq(Error("403", "Server does not support full replacement of a to-many relationship."))
    )
    Json.toJson(response)
  }
}
