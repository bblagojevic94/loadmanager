import com.google.inject.AbstractModule
import io.mainflux.loadmanager.engine.{GroupRepository, MicrogridRepository, SubscriptionRepository, Subscriptions}
import io.mainflux.loadmanager.postgres.{PgGroupRepository, PgMicrogridRepository, PgSubscriptionRepository}
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[MicrogridRepository]).to(classOf[PgMicrogridRepository])
    bind(classOf[GroupRepository]).to(classOf[PgGroupRepository])

    bindActor[Subscriptions]("subscription")
    bind(classOf[SubscriptionRepository]).to(classOf[PgSubscriptionRepository])
  }
}
