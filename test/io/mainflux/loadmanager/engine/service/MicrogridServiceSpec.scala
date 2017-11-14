package io.mainflux.loadmanager.engine.service

import io.mainflux.loadmanager.engine.Platform._
import io.mainflux.loadmanager.engine._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class MicrogridServiceSpec extends WordSpecLike with MustMatchers with MockitoSugar with ScalaFutures {

  "MicrogridService" must {
    "retrieve one microgrid" in new Fixture {
      when(microgridsRepository.retrieveOne(microgrid.id.get))
        .thenReturn(Future.successful(Option(microgrid)))

      whenReady(service.retrieveOne(microgrid.id.get)) { result =>
        result must have('id (microgrid.id),
                         'url (microgrid.url),
                         'platform (microgrid.platform),
                         'organisationId (microgrid.organisationId))
      }
    }

    "reject to return one microgrid when it does not exist" in new Fixture {
      when(microgridsRepository.retrieveOne(microgrid.id.get)).thenReturn(Future.successful(None))

      an[EntityNotFound] must be thrownBy {
        Await.result(service.retrieveOne(microgrid.id.get), 1.second)
      }
    }
  }

  trait Fixture {
    val microgridsRepository: MicrogridRepository = mock[MicrogridRepository]
    val service                                   = new MicrogridService(microgridsRepository)
    val microgrid                                 = Microgrid(Option(1), "test-url-1", OSGP, "org-1")
  }
}
