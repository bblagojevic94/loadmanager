package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{EntityNotFound, SubscriberRepository}
import io.mainflux.loadmanager.hateoas._
import play.api.http.HttpErrorHandler
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

final class Subscribers @Inject()(
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
          subscriberRepository.save(body.data.toDomain).map { savedSubscriber =>
            Created(Json.toJson(SubscriberResponse.fromDomain(savedSubscriber))).as(ContentType)
        }
      )
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    subscriberRepository
      .retrieveOne(id)
      .flatMap {
        case Some(s) =>
          val body = Json.toJson(SubscriberResponse.fromDomain(s))
          Future.successful(Ok(body).as(ContentType))
        case _ => Future.failed(EntityNotFound(s"Subscriber with id $id does not exist."))
      }
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    subscriberRepository.retrieveAll.map { subscribers =>
      val body = SubscriberCollectionResponse.fromDomain(subscribers)
      Ok(Json.toJson(body)).as(ContentType)
    }
  }

  def remove(id: Long): Action[AnyContent] = Action.async {
    subscriberRepository
      .remove(id)
      .flatMap {
        case 0 => Future.failed(EntityNotFound(s"Subscriber with id $id does not exist."))
        case _ => Future.successful(NoContent)
      }
  }

  def retrieveGroups(subscriberId: Long): Action[AnyContent] = Action.async {
    subscriberRepository
      .retrieveOne(subscriberId)
      .flatMap {
        case Some(subscriber) =>
          val body = GroupIdentifiers.fromDomain(subscriber.groups)
          Future.successful(Ok(Json.toJson(body)).as(ContentType))
        case _ => Future.failed(EntityNotFound(s"Subscriber with id $subscriberId does not exist."))
      }
  }

<<<<<<< HEAD
  def subscribe(subscriberId: Long): Action[JsValue] = Action.async(parseJsonAPI) {
    implicit request =>
      request.body
        .validate[GroupIdentifiers]
        .fold(
          errors => createErrorResponse(errors),
          body =>
            subscriberRepository.retrieveOne(subscriberId).flatMap {
              case Some(subscriber) =>
                val groupIds = body.toDomain.diff(subscriber.groups)
                subscriberRepository.subscribe(subscriberId, groupIds).map(_ => NoContent)
              case _ =>
                Future.failed(EntityNotFound(s"Subscriber with id $subscriberId does not exist."))
          }
        )
||||||| merged common ancestors
  def subscribe(subscriberId: Long): Action[JsValue] = Action.async(parseJsonAPI) { implicit request =>
    Future.successful(NoContent)
=======
  def subscribe(subscriberId: Long): Action[JsValue] = Action.async(parseJsonAPI) {
    implicit request =>
      Future.successful(NoContent)
>>>>>>> Implement basic DAO layer
  }

<<<<<<< HEAD
  def unsubscribe(subscriberId: Long): Action[JsValue] = Action.async(parseJsonAPI) {
    implicit request =>
      request.body
        .validate[GroupIdentifiers]
        .fold(
          errors => createErrorResponse(errors),
          body =>
            subscriberRepository.retrieveOne(subscriberId).flatMap {
              case Some(subscriber) =>
                val groupIds = body.toDomain.intersect(subscriber.groups)
                subscriberRepository.unsubscribe(subscriberId, groupIds).map(_ => NoContent)
              case _ =>
                Future.failed(EntityNotFound(s"Subscriber with id $subscriberId does not exist."))
          }
        )
||||||| merged common ancestors
  def unsubscribe(subscriberId: Long): Action[JsValue] = Action.async(parseJsonAPI) { implicit request =>
    Future.successful(NoContent)
=======
  def unsubscribe(subscriberId: Long): Action[JsValue] = Action.async(parseJsonAPI) {
    implicit request =>
      Future.successful(NoContent)
>>>>>>> Implement basic DAO layer
  }

  def updateGroups(subscriberId: Long): Action[AnyContent] = Action {
    Forbidden(Json.toJson(NotSupportedResponse))
  }
}
