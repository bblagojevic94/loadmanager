import java.time.LocalDateTime
import javax.inject.Singleton

import io.mainflux.loadmanager.engine.EntityNotFound
import io.mainflux.loadmanager.hateoas.{Error, ErrorResponse, JsonFormat, Meta}
import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future

@Singleton
final class ErrorHandler extends HttpErrorHandler with JsonFormat {
  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =
    Future.successful(Status(statusCode)(createResponse(statusCode, message)))

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    val response = exception match {
      case _: IllegalArgumentException => BadRequest(createResponse(400, exception.getMessage))
      case EntityNotFound(message)     => NotFound(createResponse(404, message))
      case _                           => InternalServerError(createResponse(500, exception.getMessage))
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
}
