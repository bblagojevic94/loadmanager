import com.google.inject.AbstractModule
import io.mainflux.loadmanager.engine.{GroupRepository, MicrogridRepository}
import io.mainflux.loadmanager.postgres.{PgGroupRepository, PgMicrogridRepository}

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[MicrogridRepository]).to(classOf[PgMicrogridRepository])
    bind(classOf[GroupRepository]).to(classOf[PgGroupRepository])
  }
}
