package io.mainflux.loadmanager.postgres

import java.sql.{BatchUpdateException, SQLException}

import io.mainflux.loadmanager.engine.EntityNotFound
import org.postgresql.util.PSQLException

import scala.concurrent.Future

trait PgErrorHandler {
  import PgErrorHandler._

  def handlePgErrors[A]: String => PartialFunction[Throwable, Future[A]] = entityName => {
    case ex: SQLException if ex.getSQLState == ForeignKeyViolation =>
      val msg = ex match {
        case _: PSQLException        => ex.getMessage
        case _: BatchUpdateException => ex.getNextException.getMessage
      }
      Future.failed(EntityNotFound(parseMessage(msg, entityName)))
  }

  private def parseMessage(rawMsg: String, entity: String) =
    rawMsg match {
      case Pattern(id) => s"$entity with id $id does not exist."
      case _           => "Specified relationship to a non-existent entity."
    }
}

object PgErrorHandler {
  private val ForeignKeyViolation = "23503"
  private val Pattern             = """[\s\S]* Key .*=\((\d+)\).*""".r
}
