package io.mainflux.loadmanager.controllers

import play.api.mvc.{Action, Controller}

class Application extends Controller {

  def health = Action { Ok("") }

}
