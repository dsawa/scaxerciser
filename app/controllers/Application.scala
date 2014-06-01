package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import jp.t2v.lab.play2.auth._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json.JsValue
import actors.{SolutionNotifier, StartSocket, SocketClosed}
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
      Ok(views.html.index(user)).withSession("userId" -> user.id.toString)
  }

  def initializeWS = WebSocket.async[JsValue] {
    request =>
      def errorFuture = {
        val in = Iteratee.ignore[JsValue]
        val out = Enumerator(Json.toJson("not authorized")).andThen(Enumerator.eof)
        Future {
          (in, out)
        }
      }

      request.session.get("userId") match {
        case Some(userId) => initWebSocket(userId)
        case None => errorFuture
      }
  }

  def webSocketUrl = StackAction {
    implicit request =>
      Ok(Json.obj("wsUrl" -> routes.Application.initializeWS().webSocketURL()))
  }

  private def initWebSocket(userId: String) = {
    implicit val timeout = Timeout(5 seconds)

    // enumerator dla danego użytkownika
    (SolutionNotifier.getActor ? StartSocket(userId)) map {
      enumerator =>
        // SocketClosed kiedy połączenie zakończone ze strony klienta
        (Iteratee.ignore[JsValue] map {
          _ =>
            SolutionNotifier.getActor ! SocketClosed(userId)
        }, enumerator.asInstanceOf[Enumerator[JsValue]])
    }
  }
}