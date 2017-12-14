import com.google.inject.AbstractModule
import io.mainflux.loadmanager.engine.LoadManager
import play.api.libs.concurrent.AkkaGuiceSupport

final class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit =
    bindActor[LoadManager]("load-manager")
}
