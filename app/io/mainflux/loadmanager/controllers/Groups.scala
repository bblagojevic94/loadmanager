package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine.GroupService
import io.mainflux.loadmanager.hateoas.{GroupCollectionResponse, GroupRequest, GroupResponse}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext

class Groups @Inject()(groupService: GroupService)(implicit val ec: ExecutionContext)
    extends Controller
    with ControllerAdvice {

  import io.mainflux.loadmanager.hateoas.JsonFormat._

  def create: Action[JsValue] = Action.async(JsonApiParser.json) { implicit request =>
    request.body
      .validate[GroupRequest]
      .fold(
        errors => createErrorResponse(errors),
        body => {
          (groupService.create _)
            .tupled(body.data.toDomain)
            .map(
              group =>
                Created(Json.toJson(GroupResponse.fromDomain(group))).as(JsonApiParser.JsonAPIContentType)
            )
        }
      )
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    groupService
      .retrieveAll()
      .map(
        groups =>
          Ok(Json.toJson(GroupCollectionResponse.fromDomain(groups))).as(JsonApiParser.JsonAPIContentType)
      )
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    groupService
      .retrieveOne(id)
      .map(group => Ok(Json.toJson(GroupResponse.fromDomain(group))).as(JsonApiParser.JsonAPIContentType))
  }

  def remove(id: Long): Action[AnyContent] = Action.async {
    groupService.remove(id).map(_ => NoContent)
  }
}
