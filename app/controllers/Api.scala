package controllers

import play.api.mvc._
import com.mongodb.casbah.Imports.ObjectId
import models.Solution
import actors.{SolutionNotifier, Notify}
import scala.concurrent.Future
import scaxerciser.config.APIConfig

object Api extends Controller {

  class AuthenticatedRequest[A](val appName: String, request: Request[A]) extends WrappedRequest[A](request)

  object Authenticated extends ActionBuilder[AuthenticatedRequest] {

    val AccessTokens = Map(APIConfig.ScaxerciserAnalyzeToken -> "scaxerciser_analyze")

    def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[SimpleResult]) = {
      request.headers.get("Auth-Token") match {
        case Some(token) =>
          AccessTokens.get(token) match {
            case Some(appName) =>
              block(new AuthenticatedRequest(appName, request))
            case None => Future.successful(Forbidden("Bad authorization credentials."))
          }
        case None => Future.successful(Forbidden("Missing authorization credentials."))
      }
    }
  }

  def notifyAboutResult(solutionId: String) = Authenticated {
    implicit request =>
      Solution.findOneById(new ObjectId(solutionId)) match {
        case Some(solution) =>
          if (solution.result != null) {
            SolutionNotifier.getActor ! Notify(solution)
            Ok("Notification send.")
          } else BadRequest("Solution " + solutionId + " has not been verified yet.")
        case None => NotFound("Solution " + solutionId + " not found.")
      }
  }

}