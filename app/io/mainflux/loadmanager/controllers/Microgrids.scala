package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{EntityNotFound, MicrogridRepository}
import io.mainflux.loadmanager.hateoas.{MicrogridCollectionResponse, MicrogridRequest, MicrogridResponse}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

final class Microgrids @Inject()(microgridRepository: MicrogridRepository, cc: ControllerComponents)(
    implicit val ec: ExecutionContext
) extends ApiEndpoint(cc) {

  def create: Action[JsValue] = Action.async(JsonApiParser.json) { implicit request =>
    request.body
      .validate[MicrogridRequest]
      .fold(
        errors => createErrorResponse(errors),
        body => {
          microgridRepository
            .save(body.data.toDomain)
            .map { mg =>
              Created(Json.toJson(MicrogridResponse.fromDomain(mg))).as(JsonApiParser.JsonApiContentType)
            }
        }
      )
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    microgridRepository
      .retrieveOne(id)
      .map {
        case Some(microgrid) =>
          Ok(Json.toJson(MicrogridResponse.fromDomain(microgrid))).as(JsonApiParser.JsonApiContentType)
        case _ => throw EntityNotFound(s"Microgrid with id $id does not exist")
      }
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    microgridRepository.retrieveAll.map { microgrids =>
      Ok(Json.toJson(MicrogridCollectionResponse.fromDomain(microgrids)))
        .as(JsonApiParser.JsonApiContentType)
    }
  }
}
