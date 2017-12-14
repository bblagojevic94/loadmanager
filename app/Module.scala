import com.google.inject.AbstractModule

import play.api.libs.concurrent.AkkaGuiceSupport

final class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {}
}
