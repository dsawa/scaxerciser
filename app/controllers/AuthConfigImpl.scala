package controllers

import jp.t2v.lab.play2.auth.AuthConfig
import com.mongodb.casbah.Imports._
import models.{Educator, NormalUser, Administrator, Permission, Account}
import scala.reflect._
import scala.concurrent.{Future, ExecutionContext}
import play.api.mvc.RequestHeader
import play.api.mvc.Results._
import play.api.mvc.SimpleResult

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
      case (Educator, Educator) => true
      case (Educator, NormalUser) => true
      case (NormalUser, NormalUser) => true
      case _ => false
    }
  }

  override lazy val cookieSecureOption: Boolean = play.api.Play.isProd(play.api.Play.current)

}
