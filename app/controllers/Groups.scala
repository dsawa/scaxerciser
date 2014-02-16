package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.libs.json._
import com.mongodb.casbah.Imports.ObjectId

import models.Group

object Groups extends Controller {

  def index = Action {
    val groups = Group.all().map(g => Json.parse(Group.toCompactJson(g)))
    Ok(Json.toJson(groups))
  }

  def create() = Action(parse.json) {
    implicit request =>
      (request.body \ "name").asOpt[String].map {
        name =>
          val newGroup = new Group(new ObjectId, name)
          Group.create(newGroup) match {
            case Some(id) =>
              val createdGroup = new Group(id, newGroup.name)
              Ok(Json.parse(Group.toCompactJson(createdGroup)))
            case None => UnprocessableEntity(Json.obj("error" -> "Group could not be created."))
          }
      }.getOrElse {
        BadRequest("Missing parameter [name]")
      }
  }

  def show(id: String) = Action {
    val objectId = new ObjectId(id)
    Group.findOneById(objectId) match {
      case Some(group) => Ok(Json.parse(Group.toCompactJson(group)))
      case None => NotFound(Json.obj("error" -> ("Not found group with id: " + id)))
    }
  }

  def update(id: String) = Action(parse.json) {
    implicit request =>
      (request.body \ "name").asOpt[String].map {
        newName =>
          val groupWithNewAttributes = new Group(new ObjectId(id), newName)
          val writeResult = Group.update_attributes(groupWithNewAttributes)
          if (writeResult.getN > 0) {
            Ok(Json.parse(Group.toCompactJson(groupWithNewAttributes)))
          } else {
            UnprocessableEntity(Json.obj("error" -> ("Group " + id + "could not be updated.")))
          }
      }.getOrElse {
        BadRequest("Missing parameter [name]")
      }
  }

  def delete(id: String) = Action {
    implicit request =>
      val objectId = new ObjectId(id)
      Group.findOneById(objectId) match {
        case Some(group) =>
          val writeResult = Group.remove(group)
          if (writeResult.getN > 0) {
            Ok(Json.obj("message" -> ("Group " + id + " deleted.")))
          } else {
            UnprocessableEntity(Json.obj("error" -> ("Group " + id + "could not be deleted.")))
          }
        case None => NotFound(Json.obj("error" -> ("Not found group with id: " + id)))
      }
  }
}