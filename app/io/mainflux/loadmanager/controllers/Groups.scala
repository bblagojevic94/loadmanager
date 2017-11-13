package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine.GroupService
import io.mainflux.loadmanager.hateoas.{GroupCollectionResponse, GroupRequest, GroupResponse}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsValue, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class Groups @Inject()(groupService: GroupService)(implicit val ec: ExecutionContext) extends Controller {

  import io.mainflux.loadmanager.hateoas.JsonFormat._

  def create: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body
      .validate[GroupRequest]
      .fold(
        errors => createErrorResponse(errors),
        body => {
          (groupService.createGroup _)
            .tupled(body.data.toDomain)
            .map(group => Created(Json.toJson(GroupResponse.fromDomain(group))))
        }
      )
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    groupService
      .retrieveAll()
      .map(groups => Ok(Json.toJson(GroupCollectionResponse.fromDomain(groups))))
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    groupService
      .retrieveOne(id)
      .map(group => Ok(Json.toJson(GroupResponse.fromDomain(group))))
  }

  def remove(id: Long): Action[AnyContent] = Action.async {
    groupService.remove(id).map(_ => NoContent)
  }

  private def createErrorResponse(errors: Seq[(JsPath, Seq[ValidationError])]) = {
    val invalidProperty: Option[String] = Try(errors.head._1.path.last.toString.tail).toOption
    val detail: String = invalidProperty match {
      case Some(property) =>
        s"Property $property caused ${errors.head._2.head.messages.mkString(";")}"
      case _ => s"Reason: ${errors.head._2.head.messages.mkString(";")}"
    }
    Future.failed(new IllegalArgumentException(s"Malformed JSON provided. $detail"))
  }
}
