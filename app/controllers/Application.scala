package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.{ClassTag, classTag}
import ExecutionContext.Implicits.global

import jp.t2v.lab.play2.auth._

import com.mongodb.casbah.Imports.ObjectId

import models.{Group, Account, Permission, Administrator, NormalUser}

object Application extends Controller with AuthenticationElement with LoginLogout with AuthConfigImpl {

  val loginForm = Form {
    mapping("email" -> email, "password" -> text)(Account.authenticate)(_.map(u => (u.email, "")))
      .verifying("Błędny login lub hasło", result => result.isDefined)
  }

  def login = Action { implicit request =>
      Ok(views.html.login(loginForm))
  }

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded
  }

  def authenticate = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.login(formWithErrors))),
      user => gotoLoginSucceeded(user.get.id)
    )
  }

  def index = StackAction { implicit request =>
    val user: User = loggedIn
    Ok(views.html.index(user, Group.all()))
  }
}

trait AuthConfigImpl extends AuthConfig {

  type Id = ObjectId
  type User = Account
  type Authority = Permission

  val idTag: ClassTag[Id] = classTag[Id]
  val sessionTimeoutInSeconds: Int = 3600

  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] =
    Future.successful(Account.findOneById(id))

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[SimpleResult] =
    Future.successful(Redirect(routes.Application.index))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[SimpleResult] =
    Future.successful(Redirect(routes.Application.login))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[SimpleResult] =
    Future.successful(Redirect(routes.Application.login))

  def authorizationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[SimpleResult] =
    Future.successful(Forbidden("Brak dostępu"))

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    (Permission.valueOf(user.permission), authority) match {
      case (Administrator, _) => true
      case (NormalUser, NormalUser) => true
      case _ => false
    }
  }

  override lazy val cookieSecureOption: Boolean = play.api.Play.isProd(play.api.Play.current)

}