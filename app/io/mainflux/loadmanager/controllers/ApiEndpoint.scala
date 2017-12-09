package io.mainflux.loadmanager.controllers

import io.mainflux.loadmanager.hateoas.{Error, ErrorResponse, JsonFormat}
import play.api.libs.json.{JsPath, JsValue, Json, JsonValidationError}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.Future
import scala.util.Try

abstract class ApiEndpoint(cc: ControllerComponents) extends AbstractController(cc) with JsonFormat {
  protected def createErrorResponse(errors: Seq[(JsPath, Seq[JsonValidationError])]): Future[Nothing] = {
    val invalidProperty = Try(errors.head._1.path.last.toString.tail).toOption

    val detail = invalidProperty match {
      case Some(property) =>
        s"Property $property caused ${errors.head._2.head.messages.mkString(";")}"
      case _ => s"Reason: ${errors.head._2.head.messages.mkString(";")}"
    }

    Future.failed(new IllegalArgumentException(s"Malformed JSON provided. $detail"))
  }

  val NotSupportedResponse: JsValue = {
    val response = ErrorResponse(
      errors = Seq(Error("403", "Server does not support full replacement of a to-many relationship."))
    )
    Json.toJson(response)
  }
}
