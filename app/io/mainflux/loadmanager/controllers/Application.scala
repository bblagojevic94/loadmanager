package io.mainflux.loadmanager.controllers

import play.api.mvc.{Action, Controller}

class Application extends Controller {

  def health = Action { Ok("") }

  def docs = Action {
    import Application._
    Redirect(url = SwaggerUrl, queryString = Map(UrlQuery -> Seq(OpenAPIUrl)))
  }
}

object Application {
  val SwaggerUrl = "/assets/lib/swagger-ui/index.html"
  val OpenAPIUrl = "/assets/openapi.yaml"
  val UrlQuery   = "url"
}
