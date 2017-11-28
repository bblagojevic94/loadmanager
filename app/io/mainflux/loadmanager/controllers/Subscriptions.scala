package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{EntityNotFound, GroupRepository, SubscriptionRepository}
import io.mainflux.loadmanager.hateoas._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.{ExecutionContext, Future}

class Subscriptions @Inject()(subscriptionRepository: SubscriptionRepository,
                              groupRepository: GroupRepository)(
    implicit val ec: ExecutionContext
) extends ApiEndpoint {

  import io.mainflux.loadmanager.hateoas.JsonFormat._

  def create: Action[JsValue] = Action.async(JsonApiParser.json) { implicit request =>
    request.body
      .validate[SubscriptionRequest]
      .fold(
        errors => createErrorResponse(errors),
        body => {
          val subscription = body.data.toDomain
          groupRepository
            .retrieveAllByIds(subscription.groupIds)
            .flatMap {
              case Seq() =>
                Future.failed(new IllegalArgumentException("None of specified groups does not exist"))
              case groups =>
                subscriptionRepository.save(subscription.copy(groupIds = groups.map(_.id.get))).map {
                  savedSubscription =>
                    Created(Json.toJson(SubscriptionResponse.fromDomain(savedSubscription)))
                      .as(JsonApiParser.JsonApiContentType)
                }
            }
        }
      )
  }

  def retrieveOne(id: Long): Action[AnyContent] = Action.async {
    subscriptionRepository
      .retrieveOne(id)
      .map {
        case Some(subscription) =>
          Ok(Json.toJson(SubscriptionResponse.fromDomain(subscription))).as(JsonApiParser.JsonApiContentType)
        case _ => throw EntityNotFound(s"Subscription with id $id does not exist")
      }
  }

  def retrieveAll: Action[AnyContent] = Action.async {
    subscriptionRepository.retrieveAll.map { subscriptions =>
      Ok(Json.toJson(SubscriptionCollectionResponse.fromDomain(subscriptions)))
        .as(JsonApiParser.JsonApiContentType)
    }
  }

  def remove(id: Long): Action[AnyContent] = Action.async {
    subscriptionRepository
      .remove(id)
      .map {
        case 0 => throw EntityNotFound(s"Subscriber with id $id does not exist")
        case _ => NoContent
      }
  }

  def retrieveSubscriberGroups(subscriberId: Long): Action[AnyContent] = Action.async {
    subscriptionRepository.retrieveOne(subscriberId).flatMap {
      case Some(_) =>
        groupRepository.retrieveAllBySubscription(subscriberId).map { groups =>
          Ok(Json.toJson(GroupIdentifiers.fromDomain(groups))).as(JsonApiParser.JsonApiContentType)
        }
      case _ => Future.failed(EntityNotFound(s"Subscriber with id $subscriberId does not exists"))
    }
  }

  def subscribeOnGroups(subscriberId: Long): Action[JsValue] = Action.async(JsonApiParser.json) {
    implicit request =>
      request.body
        .validate[GroupIdentifiers]
        .fold(
          errors => createErrorResponse(errors),
          body => {
            subscriptionRepository.retrieveOne(subscriberId).flatMap {
              case Some(_) =>
                subscriptionRepository.subscribeOnGroups(subscriberId, body.toDomain).map(_ => NoContent)
              case _ => Future.failed(EntityNotFound(s"Subscriber with id $subscriberId does not exists"))
            }
          }
        )
  }

  def unsubscribeFromGroups(subscriberId: Long): Action[JsValue] = Action.async(JsonApiParser.json) {
    implicit request =>
      request.body
        .validate[GroupIdentifiers]
        .fold(
          errors => createErrorResponse(errors),
          body => {
            subscriptionRepository.retrieveOne(subscriberId).flatMap {
              case Some(_) =>
                subscriptionRepository
                  .unsubscribeFromGroups(subscriberId, body.toDomain)
                  .map(_ => NoContent)
              case _ => Future.failed(EntityNotFound(s"Subscriber with id $subscriberId does not exists"))
            }
          }
        )
  }

  def updateAllGroupsOfSubscription(subscriberId: Long) = Action {
    Forbidden(Json.toJson(NotSupportedResponse))
  }

}
