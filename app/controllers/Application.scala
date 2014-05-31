package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import scala.concurrent.ExecutionContext
import jp.t2v.lab.play2.auth._
import models.Account

import play.api.libs.json._
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Enumerator
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import actors._
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json.JsValue

object Application extends Controller with AuthenticationElement with LoginLogout with AuthConfigImpl {

  val timerActor = Akka.system.actorOf(Props[TimerActor])

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

  def start = StackAction {
    implicit request =>
      val user: User = loggedIn
      timerActor ! Start(user.id.toString)
      Ok("")
  }


  def stop = StackAction {
    implicit request =>
      val user: User = loggedIn
      timerActor ! Stop(user.id.toString)
      Ok("")
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

    // using the ask pattern of akka,
    // get the enumerator for that user
    (timerActor ? StartSocket(userId)) map {
      enumerator =>

        // create a Itreatee which ignore the input and
        // and send a SocketClosed message to the actor when
        // connection is closed from the client
        (Iteratee.ignore[JsValue] map {
          _ =>
            timerActor ! SocketClosed(userId)
        }, enumerator.asInstanceOf[Enumerator[JsValue]])
    }
  }
}