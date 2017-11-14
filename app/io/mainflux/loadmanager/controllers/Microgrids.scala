package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine.MicrogridService
import io.mainflux.loadmanager.hateoas.{MicrogridCollectionResponse, MicrogridRequest, MicrogridResponse}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.ExecutionContext

class Microgrids @Inject()(microgridService: MicrogridService)(implicit val ec: ExecutionContext)
    extends Controller
    with ControllerAdvice {

  import io.mainflux.loadmanager.hateoas.JsonFormat._

  def create: Action[JsValue] = Action.async(JsonApiParser.json) { implicit request =>
    request.body
      .validate[MicrogridRequest]
      .fold(
        errors => createErrorResponse(errors),
        body => {
          microgridService
            .create(body.data.toDomain)
            .map(
              mg =>
                Created(Json.toJson(MicrogridResponse.fromDomain(mg))).as(JsonApiParser.JsonApiContentType)
            )
        }
      )
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    microgridService
      .retrieveOne(id)
      .map(
        microgrid =>
          Ok(Json.toJson(MicrogridResponse.fromDomain(microgrid))).as(JsonApiParser.JsonApiContentType)
      )
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    microgridService.retrieveAll.map(
      microgrids =>
        Ok(Json.toJson(MicrogridCollectionResponse.fromDomain(microgrids)))
          .as(JsonApiParser.JsonApiContentType)
    )
  }
}
