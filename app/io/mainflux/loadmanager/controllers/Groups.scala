package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{EntityNotFound, GroupRepository, MicrogridRepository}
import io.mainflux.loadmanager.hateoas.{GroupCollectionResponse, GroupRequest, GroupResponse}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class Groups @Inject()(groupRepository: GroupRepository, microgridRepository: MicrogridRepository)(
    implicit val ec: ExecutionContext
) extends ApiEndpoint {

  import io.mainflux.loadmanager.hateoas.JsonFormat._

  def create: Action[JsValue] = Action.async(JsonApiParser.json) { implicit request =>
    request.body
      .validate[GroupRequest]
      .fold(
        errors => createErrorResponse(errors),
        body => {
          val (group, grids) = body.data.toDomain
          microgridRepository
            .retrieveAllByIds(grids)
            .flatMap {
              case Seq() =>
                Future.failed(new IllegalArgumentException("None of specified microgrids does not exist"))
              case microgrids =>
                groupRepository.save(group.copy(grids = microgrids)).map { savedGroup =>
                  Created(Json.toJson(GroupResponse.fromDomain(savedGroup)))
                    .as(JsonApiParser.JsonApiContentType)
                }
            }
        }
      )
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    groupRepository.retrieveAll
      .map { groups =>
        Ok(Json.toJson(GroupCollectionResponse.fromDomain(groups))).as(JsonApiParser.JsonApiContentType)
      }
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    groupRepository
      .retrieveOne(id)
      .map {
        case Some(group) =>
          Ok(Json.toJson(GroupResponse.fromDomain(group))).as(JsonApiParser.JsonApiContentType)
        case _ => throw EntityNotFound(s"Group with id $id does not exists")
      }
  }

  def remove(id: Long): Action[AnyContent] = Action.async {
    groupRepository
      .remove(id)
      .map {
        case 0 => throw EntityNotFound(s"Group with id $id does not exist")
        case _ => NoContent
      }
  }
}
