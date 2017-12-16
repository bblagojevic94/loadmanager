package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine.EntityNotFound
import io.mainflux.loadmanager.hateoas._
import io.mainflux.loadmanager.postgres.SubscribersDAO
import play.api.http.HttpErrorHandler
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

final class Subscribers @Inject()(
    subscribersDAO: SubscribersDAO,
    cc: ControllerComponents,
    eh: HttpErrorHandler
)(implicit val ec: ExecutionContext)
    extends ApiEndpoint(cc, eh) {

  def create: Action[JsValue] = Action.async(parseJsonAPI) { implicit request =>
    request.body
      .validate[SubscriberRequest]
      .fold(
        errors => createErrorResponse(errors),
        body =>
          subscribersDAO.save(body.data.toDomain).map { savedSubscriber =>
            Created(Json.toJson(SubscriberResponse.fromDomain(savedSubscriber))).as(ContentType)
        }
      )
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    subscribersDAO
      .retrieveOne(id)
      .flatMap {
        case Some(s) =>
          Future.successful(Ok(Json.toJson(SubscriberResponse.fromDomain(s))).as(ContentType))
        case _ => Future.failed(EntityNotFound(s"Subscriber with id $id does not exist."))
      }
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    subscribersDAO.retrieveAll.map { subscribers =>
      Ok(Json.toJson(SubscriberCollectionResponse.fromDomain(subscribers))).as(ContentType)
    }
  }

  def remove(id: Long): Action[AnyContent] = Action.async {
    subscribersDAO
      .remove(id)
      .flatMap {
        case 0 => Future.failed(EntityNotFound(s"Subscriber with id $id does not exist."))
        case _ => Future.successful(NoContent)
      }
  }

  def retrieveGroups(subscriberId: Long): Action[AnyContent] = Action.async {
    subscribersDAO
      .retrieveOne(subscriberId)
      .flatMap {
        case Some(subscriber) =>
          val body = GroupIdentifiers.fromDomain(subscriber.groups)
          Future.successful(Ok(Json.toJson(body)).as(ContentType))
        case _ => Future.failed(EntityNotFound(s"Subscriber with id $subscriberId does not exist."))
      }
  }

  def subscribe(subscriberId: Long): Action[JsValue] = Action.async(parseJsonAPI) {
    implicit request =>
      request.body
        .validate[GroupIdentifiers]
        .fold(
          errors => createErrorResponse(errors),
          body =>
            subscribersDAO.retrieveOne(subscriberId).flatMap {
              case Some(subscriber) =>
                val groupIds = body.toDomain.diff(subscriber.groups)
                subscribersDAO.subscribe(subscriberId, groupIds).map(_ => NoContent)
              case _ =>
                Future.failed(EntityNotFound(s"Subscriber with id $subscriberId does not exist."))
          }
        )
  }

  def unsubscribe(subscriberId: Long): Action[JsValue] = Action.async(parseJsonAPI) {
    implicit request =>
      request.body
        .validate[GroupIdentifiers]
        .fold(
          errors => createErrorResponse(errors),
          body =>
            subscribersDAO.retrieveOne(subscriberId).flatMap {
              case Some(subscriber) =>
                val groupIds = body.toDomain.intersect(subscriber.groups)
                subscribersDAO.unsubscribe(subscriberId, groupIds).map(_ => NoContent)
              case _ =>
                Future.failed(EntityNotFound(s"Subscriber with id $subscriberId does not exist."))
          }
        )
  }

  def updateGroups(subscriberId: Long): Action[AnyContent] = Action {
    Forbidden(Json.toJson(NotSupportedResponse))
  }
}
