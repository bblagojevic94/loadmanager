import java.sql.BatchUpdateException
import java.time.LocalDateTime
import javax.inject.Singleton

import io.mainflux.loadmanager.engine.EntityNotFound
import io.mainflux.loadmanager.hateoas.{Error, ErrorResponse, JsonFormat, Meta}
import org.postgresql.util.PSQLException
import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
final class ErrorHandler extends HttpErrorHandler with JsonFormat {
  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =
    Future.successful(Status(statusCode)(createResponse(statusCode, message)))

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    val response = exception match {
      case _: IllegalArgumentException => BadRequest(createResponse(400, exception.getMessage))
      case EntityNotFound(message)     => NotFound(createResponse(404, message))
      case ex: PSQLException if ex.getSQLState.equals(ForeignKeyViolation) =>
        createFkViolationResponse(ex.getMessage)
      case ex: BatchUpdateException if ex.getSQLState.equals(ForeignKeyViolation) =>
        createFkViolationResponse(ex.getNextException.getMessage)
      case _ => InternalServerError(createResponse(500, exception.getMessage))
    }

    Future.successful(response.as(ContentType))
  }

  private def createResponse(code: Int, message: String) = {
    val er = ErrorResponse(
      meta = Meta(LocalDateTime.now().toString),
      errors = Seq(Error(code.toString, message))
    )

    Json.toJson(er)
  }

  private def createFkViolationResponse(message: String) = {
    val msg = message match {
      case Pattern(entity, tableName) =>
        Try(entity.toLong) match {
          case Success(id) => s"${tableName.dropRight(1).capitalize} with id $id does not exist."
          case Failure(_)  => s"Some of specified table do not exist."
        }
      case _ => "Some of specified relation entities do not exist."
    }

    NotFound(createResponse(404, msg))
  }

  private val ForeignKeyViolation = "23503"
  private val Pattern             = """[\s\S]* Key .*=\((\d+)\).*in table \"(.*)\".*""".r
}
