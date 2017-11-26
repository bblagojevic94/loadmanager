package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import io.mainflux.loadmanager.engine.{EntityNotFound, GroupRepository, SubscriptionRepository}
import io.mainflux.loadmanager.hateoas.{SubscriptionRequest, SubscriptionResponse}
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
}
