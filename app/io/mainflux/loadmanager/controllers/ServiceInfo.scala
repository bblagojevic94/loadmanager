package io.mainflux.loadmanager.controllers

import javax.inject.Inject

import play.api.mvc.{AbstractController, ControllerComponents}

final class ServiceInfo @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  import ServiceInfo._

  def health = Action { Ok("") }

  def docs = Action {
    Redirect(url = SwaggerUrl, queryString = Map(UrlQuery -> Seq(OpenAPIUrl)))
  }
}

object ServiceInfo {
  private val SwaggerUrl = "/assets/lib/swagger-ui/index.html"
  private val OpenAPIUrl = "/assets/openapi.yaml"
  private val UrlQuery   = "url"
}
