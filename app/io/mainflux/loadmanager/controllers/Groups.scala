package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine.EntityNotFound
import io.mainflux.loadmanager.hateoas.{GroupCollectionResponse, GroupRequest, GroupResponse, MicrogridIdentifiers}
import io.mainflux.loadmanager.postgres.GroupsDAO
import play.api.http.HttpErrorHandler
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

final class Groups @Inject()(
    groupsDAO: GroupsDAO,
    cc: ControllerComponents,
    eh: HttpErrorHandler
)(implicit val ec: ExecutionContext)
    extends ApiEndpoint(cc, eh) {

  def create: Action[JsValue] = Action.async(parseJsonAPI) { implicit request =>
    request.body
      .validate[GroupRequest]
      .fold(
        errors => createErrorResponse(errors),
        body => {
          groupsDAO
            .save(body.data.toDomain)
            .map { savedGroup =>
              Created(Json.toJson(GroupResponse.fromDomain(savedGroup))).as(ContentType)
            }
        }
      )
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    groupsDAO.retrieveAll
      .map { groups =>
        Ok(Json.toJson(GroupCollectionResponse.fromDomain(groups))).as(ContentType)
      }
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    groupsDAO
      .retrieveOne(id)
      .flatMap {
        case Some(group) =>
          Future.successful(Ok(Json.toJson(GroupResponse.fromDomain(group))).as(ContentType))
        case _ => Future.failed(EntityNotFound(s"Group with id $id does not exist."))
      }
  }

  def remove(id: Long): Action[AnyContent] = Action.async {
    groupsDAO
      .remove(id)
      .flatMap {
        case 0 => Future.failed(EntityNotFound(s"Group with id $id does not exist."))
        case _ => Future.successful(NoContent)
      }
  }

  def retrieveMicrogrids(groupId: Long): Action[AnyContent] = Action.async {
    groupsDAO
      .retrieveOne(groupId)
      .flatMap {
        case Some(group) =>
          val body = MicrogridIdentifiers.fromDomain(group.microgrids)
          Future.successful(Ok(Json.toJson(body)).as(ContentType))
        case _ => Future.failed(EntityNotFound(s"Group with id $groupId does not exist."))
      }
  }

  def addMicrogrids(groupId: Long): Action[JsValue] = Action.async(parseJsonAPI) {
    implicit request =>
      request.body
        .validate[MicrogridIdentifiers]
        .fold(
          errors => createErrorResponse(errors),
          body =>
            groupsDAO.retrieveOne(groupId).flatMap {
              case Some(group) =>
                val mgIds = body.toDomain.diff(group.microgrids)
                groupsDAO.addMicrogrids(groupId, mgIds).map(_ => NoContent)
              case _ => Future.failed(EntityNotFound(s"Group with id $groupId does not exist."))
          }
        )
  }

  def removeMicrogrids(groupId: Long): Action[JsValue] = Action.async(parseJsonAPI) {
    implicit request =>
      request.body
        .validate[MicrogridIdentifiers]
        .fold(
          errors => createErrorResponse(errors),
          body =>
            groupsDAO.retrieveOne(groupId).flatMap {
              case Some(group) =>
                val mgIds = body.toDomain.intersect(group.microgrids)
                groupsDAO.removeMicrogrids(groupId, mgIds).map(_ => NoContent)
              case _ => Future.failed(EntityNotFound(s"Group with id $groupId does not exist."))
          }
        )
  }

  def updateMicrogrids(groupId: Long) = Action {
    Forbidden(Json.toJson(NotSupportedResponse))
  }
}
