package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine._
import io.mainflux.loadmanager.hateoas._
import play.api.http.HttpErrorHandler
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

final class Subscriptions @Inject()(subscriptionService: SubscriptionService,
                                    groupRepository: GroupRepository,
                                    cc: ControllerComponents,
                                    eh: HttpErrorHandler)(implicit val ec: ExecutionContext)
    extends ApiEndpoint(cc, eh) {

  def create: Action[JsValue] = Action.async(parseJsonAPI) { implicit request =>
    request.body
      .validate[SubscriptionRequest]
      .fold(
        errors => createErrorResponse(errors),
        body =>
          subscriptionService.create(body.data.toDomain).map { savedSubscription =>
            Created(Json.toJson(SubscriptionResponse.fromDomain(savedSubscription)))
              .as(ContentType)
        }
      )
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    subscriptionService.retrieveOne(id).map { subscription =>
      Ok(Json.toJson(SubscriptionResponse.fromDomain(subscription))).as(ContentType)
    }
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    subscriptionService.retrieveAll.map { subscriptions =>
      Ok(Json.toJson(SubscriptionCollectionResponse.fromDomain(subscriptions)))
        .as(ContentType)
    }
  }

  def remove(id: Long): Action[AnyContent] = Action.async {
    subscriptionService.remove(id).map(_ => NoContent)
  }

  def retrieveSubscriberGroups(subscriberId: Long): Action[AnyContent] = Action.async {
    subscriptionService.retrieveSubscriberGroups(subscriberId).map { groups =>
      Ok(Json.toJson(GroupIdentifiers.fromDomain(groups))).as(ContentType)
    }
  }

  def subscribeOnGroups(subscriberId: Long): Action[JsValue] = Action.async(parseJsonAPI) {
    implicit request =>
      request.body
        .validate[GroupIdentifiers]
        .fold(
          errors => createErrorResponse(errors),
          body => subscriptionService.subscribeOnGroup(subscriberId, body.toDomain).map(_ => NoContent)
        )
  }

  def unsubscribeFromGroups(subscriberId: Long): Action[JsValue] = Action.async(parseJsonAPI) {
    implicit request =>
      request.body
        .validate[GroupIdentifiers]
        .fold(
          errors => createErrorResponse(errors),
          body => subscriptionService.unsubscribeFromGroup(subscriberId, body.toDomain).map(_ => NoContent)
        )
  }

  def updateAllGroupsOfSubscription(subscriberId: Long): Action[AnyContent] = Action {
    Forbidden(Json.toJson(NotSupportedResponse))
  }

}
