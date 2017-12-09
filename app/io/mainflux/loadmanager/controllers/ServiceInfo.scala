package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import play.api.mvc.{AbstractController, ControllerComponents}

final class ServiceInfo @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def health = Action { Ok("") }

  def docs = Action {
    import ServiceInfo._
    Redirect(url = SwaggerUrl, queryString = Map(UrlQuery -> Seq(OpenAPIUrl)))
  }
}

object ServiceInfo {
  val SwaggerUrl = "/assets/lib/swagger-ui/index.html"
  val OpenAPIUrl = "/assets/openapi.yaml"
  val UrlQuery   = "url"
}
