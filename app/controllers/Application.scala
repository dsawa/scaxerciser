package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import jp.t2v.lab.play2.auth._
import models.Account

object Application extends Controller with AuthenticationElement with LoginLogout with AuthConfigImpl {

  val loginForm = Form {
    mapping("email" -> email, "password" -> text)(Account.authenticate)(_.map(u => (u.email, "")))
      .verifying("Błędny login lub hasło", result => result.isDefined)
  }

  def login = Action {
    implicit request =>
      Ok(views.html.login(loginForm))
  }

  def logout = Action.async {
    implicit request =>
      gotoLogoutSucceeded
  }

  def authenticate = Action.async {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(views.html.login(formWithErrors))),
        user => gotoLoginSucceeded(user.get.id)
      )
  }

  def index = StackAction {
    implicit request =>
      val user: User = loggedIn
      Ok(views.html.index(user))
  }
}