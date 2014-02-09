package controllers

import play.api.mvc._

import models.Group

object Application extends Controller {

  def index = Action {
    Ok(views.html.index(Group.all().toList))
  }

}