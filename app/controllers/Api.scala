package controllers

import play.api.mvc._
import com.mongodb.casbah.Imports._
import models.Solution
import actors.{SolutionNotifier, Notify}

object Api extends Controller {

  def notifyAboutResult(solutionId: String) = Action {
    implicit request =>
      Solution.findOneById(new ObjectId(solutionId)) match {
        case Some(solution) =>
          if(solution.result != null) {
            SolutionNotifier.getActor ! Notify(solution)
            Ok("Notification send.")
          } else BadRequest("Solution " + solutionId + " has not been verified yet.")
        case None => NotFound("Solution " + solutionId + " not found.")
      }
  }

}