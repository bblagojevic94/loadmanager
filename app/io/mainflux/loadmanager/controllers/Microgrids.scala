package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import play.api.http.HttpErrorHandler
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

final class Microgrids @Inject()(cc: ControllerComponents, eh: HttpErrorHandler)(implicit val ec: ExecutionContext)
    extends ApiEndpoint(cc, eh) {

  def create: Action[JsValue] = Action.async(parseJsonAPI) { implicit request =>
    Future.successful(Created("").as(ContentType))
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    Future.successful(Ok("").as(ContentType))
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    Future.successful(Ok("").as(ContentType))
  }
}
