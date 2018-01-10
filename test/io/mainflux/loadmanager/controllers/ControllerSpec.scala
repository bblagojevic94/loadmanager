package io.mainflux.loadmanager.controllers

import io.mainflux.loadmanager.hateoas.JsonFormat
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results

abstract class ControllerSpec
    extends FlatSpec
    with Matchers
    with MockitoSugar
    with Results
    with JsonFormat {
  lazy val app: Application = new GuiceApplicationBuilder()
    .configure("slick.dbs.default.db.connectionPool" -> "disabled")
    .configure("play.evolutions.enabled" -> "false")
    .build()
}
