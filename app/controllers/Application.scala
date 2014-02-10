package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._

import models.{Group, Account}

object Application extends Controller {

  val loginForm = Form {
    mapping("email" -> email, "password" -> text)(Account.authenticate)(_.map(u => (u.email, "")))
      .verifying("Błędny login lub hasło", result => result.isDefined)
  }

  def index = Action {
    Ok(views.html.index(Group.all()))
  }

  def login = Action { implicit request =>
      Ok(views.html.login(loginForm))
  }

}