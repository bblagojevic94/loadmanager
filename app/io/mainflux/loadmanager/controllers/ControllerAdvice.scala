package io.mainflux.loadmanager.controllers

import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath

import scala.concurrent.Future
import scala.util.Try

trait ControllerAdvice {
  protected def createErrorResponse(errors: Seq[(JsPath, Seq[ValidationError])]): Future[Nothing] = {
    val invalidProperty: Option[String] = Try(errors.head._1.path.last.toString.tail).toOption
    val detail: String = invalidProperty match {
      case Some(property) =>
        s"Property $property caused ${errors.head._2.head.messages.mkString(";")}"
      case _ => s"Reason: ${errors.head._2.head.messages.mkString(";")}"
    }
    Future.failed(new IllegalArgumentException(s"Malformed JSON provided. $detail"))
  }
}
