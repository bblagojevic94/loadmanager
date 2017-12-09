package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{GroupService, MicrogridRepository}
import io.mainflux.loadmanager.hateoas._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext

final class Groups @Inject()(groupService: GroupService,
                             microgridRepository: MicrogridRepository,
                             cc: ControllerComponents)(implicit val ec: ExecutionContext)
    extends ApiEndpoint(cc) {

  def create: Action[JsValue] = Action.async(JsonApiParser.json) { implicit request =>
    request.body
      .validate[GroupRequest]
      .fold(
        errors => createErrorResponse(errors),
        body => {
          val (group, grids) = body.data.toDomain
          groupService.create(group, grids).map { savedGroup =>
            Created(Json.toJson(GroupResponse.fromDomain(savedGroup))).as(JsonApiParser.JsonApiContentType)
          }
        }
      )
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    groupService.retrieveAll
      .map { groups =>
        Ok(Json.toJson(GroupCollectionResponse.fromDomain(groups))).as(JsonApiParser.JsonApiContentType)
      }
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    groupService
      .retrieveOne(id)
      .map { group =>
        Ok(Json.toJson(GroupResponse.fromDomain(group))).as(JsonApiParser.JsonApiContentType)
      }
  }

  def remove(id: Long): Action[AnyContent] = Action.async {
    groupService.remove(id).map(_ => NoContent)
  }

  def retrieveGroupMicrogrids(groupId: Long): Action[AnyContent] = Action.async {
    groupService.retrieveGroupMicrogrids(groupId).map { microgrids =>
      Ok(Json.toJson(MicrogridIdentifiers.fromDomain(microgrids))).as(JsonApiParser.JsonApiContentType)
    }
  }

  def addMicrogridsInGroup(groupId: Long): Action[JsValue] = Action.async(JsonApiParser.json) {
    implicit request =>
      request.body
        .validate[MicrogridIdentifiers]
        .fold(
          errors => createErrorResponse(errors),
          body => groupService.addMicrogrids(groupId, body.toDomain).map(_ => NoContent)
        )
  }

  def removeMicrogridsFromGroup(groupId: Long): Action[JsValue] = Action.async(JsonApiParser.json) {
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
