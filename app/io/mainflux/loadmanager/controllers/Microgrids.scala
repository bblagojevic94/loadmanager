package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine.EntityNotFound
import io.mainflux.loadmanager.hateoas.{MicrogridCollectionResponse, MicrogridRequest, MicrogridResponse}
import io.mainflux.loadmanager.postgres.MicrogridsDAO
import play.api.http.HttpErrorHandler
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

final class Microgrids @Inject()(
    microgridsDAO: MicrogridsDAO,
    cc: ControllerComponents,
    eh: HttpErrorHandler
)(implicit val ec: ExecutionContext)
    extends ApiEndpoint(cc, eh) {

  def create: Action[JsValue] = Action.async(parseJsonAPI) { implicit request =>
    request.body
      .validate[MicrogridRequest]
      .fold(
        errors => createErrorResponse(errors),
        body => {
          microgridsDAO
            .save(body.data.toDomain)
            .map { mg =>
              Created(Json.toJson(MicrogridResponse.fromDomain(mg))).as(ContentType)
            }
        }
      )
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    microgridsDAO
      .retrieveOne(id)
      .map {
        case Some(microgrid) =>
          Ok(Json.toJson(MicrogridResponse.fromDomain(microgrid))).as(ContentType)
        case _ => throw EntityNotFound(s"Microgrid with id $id does not exist.")
      }
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    microgridsDAO.retrieveAll.map { microgrids =>
      Ok(Json.toJson(MicrogridCollectionResponse.fromDomain(microgrids))).as(ContentType)
    }
  }

  def remove(id: Long): Action[AnyContent] = Action.async {
    microgridsDAO
      .remove(id)
      .flatMap {
        case 0 => Future.failed(EntityNotFound(s"Microgrid with id $id does not exist."))
        case _ => Future.successful(NoContent)
      }
  }
}
