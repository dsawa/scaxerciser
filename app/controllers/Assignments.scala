package controllers

import java.io.File
import play.api.Play
import play.api.Play.current
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.api.libs.iteratee.Enumerator
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import com.mongodb.casbah.Imports._
import jp.t2v.lab.play2.auth.AuthElement
import models._

object Assignments extends Controller with AuthElement with AuthConfigImpl {

  def index(groupId: String) = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      val assignments = Assignment.findByGroupId(new ObjectId(groupId)).map(a => Json.parse(Assignment.toCompactJson(a)))
      Ok(Json.toJson(assignments))
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

  def show(groupId: String, id: String) = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      val query = MongoDBObject("groupId" -> new ObjectId(groupId), "_id" -> new ObjectId(id))
      Assignment.findOne(query) match {
        case Some(assignment) => Ok(Json.parse(Assignment.toCompactJson(assignment)))
        case None => NotFound("Assignment " + id + " not found in group " + groupId)
      }
  }

  def update(groupId: String, id: String) = StackAction(parse.json, AuthorityKey -> Administrator) {
    implicit request =>
      val query = MongoDBObject("groupId" -> new ObjectId(groupId), "_id" -> new ObjectId(id))
      Assignment.findOne(query) match {
        case Some(assignment) =>
          val title = (request.body \ "title").asOpt[String]
          val exercises = (request.body \ "exercises").asOpt[List[Exercise]]

          if (title.isDefined && exercises.isDefined && exercises.get.size > 0) {
            val assignmentToUpdate = assignment.copy(title = title.get, exercises = exercises.get)
            val writeResult = Assignment.save(assignmentToUpdate)
            if (writeResult.getN > 0)
              Ok(Json.parse(Assignment.toCompactJson(assignmentToUpdate)))
            else
              UnprocessableEntity("Assignment could not be updated")
          } else {
            BadRequest("Required parameters [title, exercises]")
          }
        case None => NotFound("Assignment " + id + " not found in group " + groupId)
      }
  }

  def addProject(groupId: String, id: String) = StackAction(parse.multipartFormData, AuthorityKey -> Administrator) {
    implicit request =>
      val query = MongoDBObject("groupId" -> new ObjectId(groupId), "_id" -> new ObjectId(id))
      Assignment.findOne(query) match {
        case Some(assignment) =>
          request.body.file("projectFile").map {
            projectFile =>
              val tmpFile = new File(Play.application.path + "/tmp/" + projectFile.filename)
              projectFile.ref.moveTo(tmpFile, replace = true)
              Assignment.addProject(assignment.copy(enabled = true), tmpFile, contentType = projectFile.contentType.get) match {
                case Some(projectId) =>
                  tmpFile.delete()
                  Ok(Json.obj("id" -> projectId.toString))
                case None =>
                  tmpFile.delete()
                  UnprocessableEntity("Project could not be added")
              }
          }.getOrElse {
            BadRequest("No file")
          }
        case None => NotFound("Assignment " + id + " not found in group " + groupId)
      }
  }

  def getProject(groupId: String, id: String) = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      val query = MongoDBObject("groupId" -> new ObjectId(groupId), "_id" -> new ObjectId(id))
      Assignment.findOne(query) match {
        case Some(assignment) =>
          Assignment.getProject(assignment) match {
            case Some(gridfsdbfile) =>
              val dataContent = Enumerator.fromStream(gridfsdbfile.inputStream)
              val filename = gridfsdbfile.filename.getOrElse { "project" }
              val contentType = gridfsdbfile.contentType.getOrElse { "text/plain" }
              Ok.chunked(dataContent).withHeaders(CONTENT_DISPOSITION -> ("filename=" + filename)).as(contentType)
            case None => NotFound("Not found project for assignment with id " + id)
          }
        case None => NotFound("Assignment " + id + " not found in group " + groupId)
      }
  }

}