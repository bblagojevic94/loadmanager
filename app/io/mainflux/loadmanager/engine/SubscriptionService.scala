package io.mainflux.loadmanager.engine

import javax.inject.{Named, Singleton}

import akka.actor.ActorRef
import com.google.inject.Inject
import io.mainflux.loadmanager.engine.Subscriptions.{RemoveSubscriber, SubscribeOnGroup, UnsubscribeFromGroup}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionService @Inject()(
    subscriptionRepository: SubscriptionRepository,
    groupRepository: GroupRepository,
    @Named("subscription") subscription: ActorRef
)(implicit ec: ExecutionContext) {

  def create(sub: Subscription): Future[Subscription] =
    groupRepository
      .retrieveAll(sub.groupIds.toSet)
      .flatMap {
        case Seq() =>
          Future.failed(new IllegalArgumentException("None of specified groups does not exist"))
        case groups =>
          subscriptionRepository.save(sub.copy(groupIds = groups.map(_.id.get))).map { saved =>
            saved.groupIds.foreach { groupId =>
              subscription ! SubscribeOnGroup(groupId, saved)
            }
            saved
          }
      }

  def retrieveOne(id: Long): Future[Subscription] =
    subscriptionRepository
      .retrieveOne(id)
      .map(_.getOrElse(throw EntityNotFound(s"Subscription with id $id does not exist")))

  def retrieveAll: Future[Seq[Subscription]] = subscriptionRepository.retrieveAll

  def remove(id: Long): Future[Unit] =
    subscriptionRepository
      .remove(id)
      .map {
        case 0 => throw EntityNotFound(s"Subscriber with id $id does not exist")
        case _ => subscription ! RemoveSubscriber(id)
      }

  def retrieveSubscriberGroups(subscriberId: Long): Future[Seq[Long]] =
    subscriptionRepository.retrieveOne(subscriberId).flatMap {
      case Some(_) => groupRepository.retrieveAllBySubscription(subscriberId)
      case _       => Future.failed(EntityNotFound(s"Subscriber with id $subscriberId does not exists"))
    }

  def subscribeOnGroup(subscriberId: Long, groupIds: Seq[Long]): Future[Unit] =
    subscriptionRepository.retrieveOne(subscriberId).flatMap {
      case Some(sub) =>
        subscriptionRepository
          .subscribeOnGroups(subscriberId, groupIds)
          .map(_.foreach { groupId =>
            subscription ! SubscribeOnGroup(groupId, sub)
          })
      case _ => Future.failed(EntityNotFound(s"Subscriber with id $subscriberId does not exists"))
    }

  def unsubscribeFromGroup(subscriberId: Long, groupIds: Seq[Long]): Future[Unit] =
    subscriptionRepository.retrieveOne(subscriberId).flatMap {
      case Some(_) =>
        subscriptionRepository
          .unsubscribeFromGroups(subscriberId, groupIds)
          .map(_.foreach { groupId =>
            subscription ! UnsubscribeFromGroup(subscriberId, groupId)
          })
      case _ => Future.failed(EntityNotFound(s"Subscriber with id $subscriberId does not exists"))
    }
}
