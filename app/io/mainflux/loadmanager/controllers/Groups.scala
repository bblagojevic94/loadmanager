package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import play.api.http.HttpErrorHandler
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

final class Groups @Inject()(cc: ControllerComponents, eh: HttpErrorHandler)(implicit val ec: ExecutionContext)
    extends ApiEndpoint(cc, eh) {

  def create: Action[JsValue] = Action.async(parseJsonAPI) { implicit request =>
    Future.successful(Created("").as(ContentType))
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    Future.successful(Ok("").as(ContentType))
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    Future.successful(Ok("").as(ContentType))
  }

  def remove(id: Long): Action[AnyContent] = Action.async {
    Future.successful(NoContent)
  }

  def retrieveMicrogrids(groupId: Long): Action[AnyContent] = Action.async {
    Future.successful(Ok("").as(ContentType))
  }

  def addMicrogrids(groupId: Long): Action[JsValue] = Action.async(parseJsonAPI) { implicit request =>
    Future.successful(NoContent)
  }

  def removeMicrogrids(groupId: Long): Action[JsValue] = Action.async(parseJsonAPI) { implicit request =>
    Future.successful(NoContent)
  }

  def updateMicrogrids(groupId: Long) = Action {
    Forbidden(Json.toJson(NotSupportedResponse))
  }
}
