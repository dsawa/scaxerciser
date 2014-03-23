package controllers

import play.api.libs.json.Json
import play.api.mvc.Controller
import com.mongodb.casbah.Imports.ObjectId
import jp.t2v.lab.play2.auth.AuthElement
import models._

object Assignments extends Controller with AuthElement with AuthConfigImpl {

  def index = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      Ok("")
  }

  def create(groupId: String) = StackAction(parse.json, AuthorityKey -> Administrator) {
    implicit request =>
      Group.findOneById(new ObjectId(groupId)) match {
        case Some(group) =>
          val title = (request.body \ "title").asOpt[String]
          val exercises = (request.body \ "exercises").asOpt[List[Exercise]]

          if (title.isDefined && exercises.isDefined && exercises.get.size > 0) {
            val newAssignment = Assignment(new ObjectId, title.get, exercises.get, group.id)
            Assignment.create(newAssignment) match {
              case Some(id) => Ok(Json.parse(Assignment.toCompactJson(newAssignment)))
              case None => UnprocessableEntity("Assignment could not be created.")
            }
          } else {
            BadRequest("Required parameters [title, exercises]")
          }
        case None => NotFound("Group " + groupId + " not found")
      }
  }

}