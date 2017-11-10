import com.google.inject.AbstractModule

import io.mainflux.loadmanager.engine.persistence.{
  GroupRepository,
  MicrogridRepository,
  PgGroupRepository,
  PgMicrogridRepository
}
import io.mainflux.loadmanager.engine.service.GroupService

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[MicrogridRepository]).to(classOf[PgMicrogridRepository])
    bind(classOf[GroupRepository]).to(classOf[PgGroupRepository])

    bind(classOf[GroupService]).asEagerSingleton()
  }
}