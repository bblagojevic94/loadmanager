package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine._
import io.mainflux.loadmanager.hateoas._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.ExecutionContext

class Subscriptions @Inject()(subscriptionService: SubscriptionService, groupRepository: GroupRepository)(
    implicit val ec: ExecutionContext
) extends ApiEndpoint {

  import io.mainflux.loadmanager.hateoas.JsonFormat._

  def create: Action[JsValue] = Action.async(JsonApiParser.json) { implicit request =>
    request.body
      .validate[SubscriptionRequest]
      .fold(
        errors => createErrorResponse(errors),
        body =>
          subscriptionService.create(body.data.toDomain).map { savedSubscription =>
            Created(Json.toJson(SubscriptionResponse.fromDomain(savedSubscription)))
              .as(JsonApiParser.JsonApiContentType)
        }
      )
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    subscriptionService.retrieveOne(id).map { subscription =>
      Ok(Json.toJson(SubscriptionResponse.fromDomain(subscription))).as(JsonApiParser.JsonApiContentType)
    }
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    subscriptionService.retrieveAll.map { subscriptions =>
      Ok(Json.toJson(SubscriptionCollectionResponse.fromDomain(subscriptions)))
        .as(JsonApiParser.JsonApiContentType)
    }
  }

  def remove(id: Long): Action[AnyContent] = Action.async {
    subscriptionService.remove(id).map(_ => NoContent)
  }

  def retrieveSubscriberGroups(subscriberId: Long): Action[AnyContent] = Action.async {
    subscriptionService.retrieveSubscriberGroups(subscriberId).map { groups =>
      Ok(Json.toJson(GroupIdentifiers.fromDomain(groups))).as(JsonApiParser.JsonApiContentType)
    }
  }

  def subscribeOnGroups(subscriberId: Long): Action[JsValue] = Action.async(JsonApiParser.json) {
    implicit request =>
      request.body
        .validate[GroupIdentifiers]
        .fold(
          errors => createErrorResponse(errors),
          body => subscriptionService.subscribeOnGroup(subscriberId, body.toDomain).map(_ => NoContent)
        )
  }

  def unsubscribeFromGroups(subscriberId: Long): Action[JsValue] = Action.async(JsonApiParser.json) {
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
