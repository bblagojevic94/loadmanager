import com.google.inject.AbstractModule
import io.mainflux.loadmanager.engine.{GroupRepository, MicrogridRepository, SubscriberRepository}
import io.mainflux.loadmanager.postgres.{GroupsDAO, MicrogridsDAO, SubscribersDAO}
import play.api.libs.concurrent.AkkaGuiceSupport

final class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[MicrogridRepository]).to(classOf[MicrogridsDAO])
    bind(classOf[GroupRepository]).to(classOf[GroupsDAO])
    bind(classOf[SubscriberRepository]).to(classOf[SubscribersDAO])
  }
}
