package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{EntityNotFound, MicrogridRepository}
import io.mainflux.loadmanager.hateoas.{MicrogridCollectionResponse, MicrogridRequest, MicrogridResponse}
import play.api.http.HttpErrorHandler
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

final class Microgrids @Inject()(
    microgridRepository: MicrogridRepository,
    cc: ControllerComponents,
    eh: HttpErrorHandler
)(implicit val ec: ExecutionContext)
    extends ApiEndpoint(cc, eh) {

  def create: Action[JsValue] = Action.async(parseJsonAPI) { implicit request =>
    request.body
      .validate[MicrogridRequest]
      .fold(
        errors => createErrorResponse(errors),
        body =>
          microgridRepository
            .save(body.data.toDomain)
            .map { mg =>
              val body = Json.toJson(MicrogridResponse.fromDomain(mg))
              Created(body).as(ContentType)
          }
      )
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    microgridRepository
      .retrieveOne(id)
      .flatMap {
        case Some(mg) =>
          val body = Json.toJson(MicrogridResponse.fromDomain(mg))
          Future.successful(Ok(body).as(ContentType))
        case _ => Future.failed(EntityNotFound(s"Microgrid with id $id does not exist."))
      }
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    microgridRepository.retrieveAll.map { entities =>
      val body = Json.toJson(MicrogridCollectionResponse.fromDomain(entities))
      Ok(body).as(ContentType)
    }
  }

  def remove(id: Long): Action[AnyContent] = Action.async {
    microgridRepository
      .remove(id)
      .flatMap {
        case 0 => Future.failed(EntityNotFound(s"Microgrid with id $id does not exist."))
        case _ => Future.successful(NoContent)
      }
  }
}
