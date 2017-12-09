package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{GroupService, MicrogridRepository}
import io.mainflux.loadmanager.hateoas._
import play.api.http.HttpErrorHandler
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext

final class Groups @Inject()(groupService: GroupService,
                             microgridRepository: MicrogridRepository,
                             cc: ControllerComponents,
                             eh: HttpErrorHandler)(implicit val ec: ExecutionContext)
    extends ApiEndpoint(cc, eh) {

  def create: Action[JsValue] = Action.async(parseJsonAPI) { implicit request =>
    request.body
      .validate[GroupRequest]
      .fold(
        errors => createErrorResponse(errors),
        body => {
          val (group, grids) = body.data.toDomain
          groupService.create(group, grids).map { savedGroup =>
            Created(Json.toJson(GroupResponse.fromDomain(savedGroup))).as(ContentType)
          }
        }
      )
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    groupService.retrieveAll
      .map { groups =>
        Ok(Json.toJson(GroupCollectionResponse.fromDomain(groups))).as(ContentType)
      }
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    groupService
      .retrieveOne(id)
      .map { group =>
        Ok(Json.toJson(GroupResponse.fromDomain(group))).as(ContentType)
      }
  }

  def remove(id: Long): Action[AnyContent] = Action.async {
    groupService.remove(id).map(_ => NoContent)
  }

  def retrieveGroupMicrogrids(groupId: Long): Action[AnyContent] = Action.async {
    groupService.retrieveGroupMicrogrids(groupId).map { microgrids =>
      Ok(Json.toJson(MicrogridIdentifiers.fromDomain(microgrids))).as(ContentType)
    }
  }

  def addMicrogridsInGroup(groupId: Long): Action[JsValue] = Action.async(parseJsonAPI) { implicit request =>
    request.body
      .validate[MicrogridIdentifiers]
      .fold(
        errors => createErrorResponse(errors),
        body => groupService.addMicrogrids(groupId, body.toDomain).map(_ => NoContent)
      )
  }

  def removeMicrogridsFromGroup(groupId: Long): Action[JsValue] = Action.async(parseJsonAPI) {
    implicit request =>
      request.body
        .validate[MicrogridIdentifiers]
        .fold(
          errors => createErrorResponse(errors),
          body => groupService.removeMicrogrids(groupId, body.toDomain).map(_ => NoContent)
        )
  }

  def updateAllMicrogridsOfGroup(groupId: Long) = Action {
    Forbidden(Json.toJson(NotSupportedResponse))
  }
}
