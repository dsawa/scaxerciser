package controllers

import play.api.mvc.Controller
import play.api.libs.json._
import com.mongodb.casbah.Imports.ObjectId
import jp.t2v.lab.play2.auth.AuthElement
import models.{Group, Assignment, NormalUser, Administrator}

object Groups extends Controller with AuthElement with AuthConfigImpl {

  def index = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      val currentUser: User = loggedIn
      val groups = currentUser.groups.all.map(dbo => Group.toObject(dbo))
      Ok(Group.toCompactJSONArray(groups)).withHeaders("Content-Type" -> "application/json")
  }

  def create = StackAction(parse.json, AuthorityKey -> Administrator) {
    implicit request =>
      (request.body \ "name").asOpt[String].map {
        name =>
          val currentUser: User = loggedIn
          val newGroup = new Group(new ObjectId, name)
          val writeResult = currentUser.groups.create(newGroup)
          if (writeResult.getN > 0)
            Ok(Json.parse(Group.toCompactJson(newGroup)))
          else
            UnprocessableEntity("Group could not be created.")
      }.getOrElse {
        BadRequest("Missing parameter [name]")
      }
  }

  def show(id: String) = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      val objectId = new ObjectId(id)
      Group.findOneById(objectId) match {
        case Some(group) => Ok(Json.parse(Group.toCompactJson(group)))
        case None => NotFound("Group " + id + " not found")
      }
  }

  def update(id: String) = StackAction(parse.json, AuthorityKey -> Administrator) {
    implicit request =>
      (request.body \ "name").asOpt[String].map {
        newName =>
          Group.findOneById(new ObjectId(id)) match {
            case Some(group) =>
              val toUpdate = group.copy(name = newName)
              val writeResult = Group.updateAttributes(toUpdate)
              if (writeResult.getN > 0)
                Ok(Json.parse(Group.toCompactJson(toUpdate)))
              else
                UnprocessableEntity("Group " + id + " could not be updated.")
            case None => NotFound("Group " + id + " not found")
          }
      }.getOrElse {
        BadRequest("Missing parameter [name]")
      }
  }

  def delete(id: String) = StackAction(AuthorityKey -> Administrator) {
    implicit request =>
      val groupId = new ObjectId(id)
      Group.findOneById(groupId) match {
        case Some(group) =>
          val currentUser: User = loggedIn
          val writeResult = currentUser.groups.destroy(group)
          if (writeResult.getN > 0) {
            Assignment.removeAllProjectsFromGroup(groupId)
            group.assignments.destroyAll
            Ok(Json.obj("message" -> ("Group " + id + " deleted.")))
          } else
            UnprocessableEntity("Group " + id + "could not be deleted.")
        case None => NotFound("Group " + id + " not found.")
      }
  }
}