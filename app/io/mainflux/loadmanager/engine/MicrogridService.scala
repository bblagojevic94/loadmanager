package io.mainflux.loadmanager.engine

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MicrogridService @Inject()(microgridRepository: MicrogridRepository)(implicit ec: ExecutionContext) {

  def create(microgrid: Microgrid): Future[Microgrid] = microgridRepository.save(microgrid)

  def findOne(id: Long): Future[Microgrid] =
    microgridRepository.retrieveOne(id).map {
      case Some(microgrid) => microgrid
      case None            => throw EntityNotFound(s"Microgrid with id $id does not exist")
    }

  def findAll: Future[Seq[Microgrid]] = microgridRepository.retrieveAll
}
