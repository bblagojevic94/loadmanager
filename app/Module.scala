import com.google.inject.AbstractModule
import io.mainflux.loadmanager.engine.{GroupRepository, MicrogridRepository, SubscriberRepository}
import io.mainflux.loadmanager.postgres.{PgGroupRepository, PgMicrogridRepository, PgSubscriberRepository}
import play.api.libs.concurrent.AkkaGuiceSupport

final class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[MicrogridRepository]).to(classOf[PgMicrogridRepository])
    bind(classOf[GroupRepository]).to(classOf[PgGroupRepository])
    bind(classOf[SubscriberRepository]).to(classOf[PgSubscriberRepository])
  }
}
