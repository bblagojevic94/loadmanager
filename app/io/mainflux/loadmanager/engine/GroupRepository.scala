package io.mainflux.loadmanager.engine

import scala.concurrent.Future

trait GroupRepository {

  def save(group: Group): Future[Group]

}
