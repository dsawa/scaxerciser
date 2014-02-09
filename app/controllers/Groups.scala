package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.libs.json._
import org.bson.types.ObjectId

import models.Group

object Groups extends Controller {

  def index = Action {
    val groups = Group.all().map(g => Json.parse(Group.toCompactJson(g)))
    Ok(Json.toJson(groups))
  }

  def create() = Action(parse.json) {
    request =>
      (request.body \ "name").asOpt[String].map {
        name =>
          val createdGroup = Group.create(name)
          Ok(Json.parse(Group.toCompactJson(createdGroup)))
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
    request =>
      (request.body \ "name").asOpt[String].map {
        name =>
          Group.update_attributes(id, name)
          Ok(Json.obj("success" -> true, "message" -> ("Group with id: " + id + " updated.")))
      }.getOrElse {
        BadRequest("Missing parameter [name]")
      }
  }

  def delete(id: String) = Action(parse.json) {
    request =>
      (request.body \ "id").asOpt[String].map {
        id =>
          Group.delete(id)
          Ok(Json.obj("success" -> true, "message" -> ("Group with id: " + id + " deleted.")))
      }.getOrElse {
        BadRequest("Missing parameter [id]")
      }
  }
}