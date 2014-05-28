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
      val currentUser: User = loggedIn
      Group.findOneById(new ObjectId(groupId)) match {
        case Some(group) =>
          val query = {
            if (Group.hasUserPermission(group, currentUser, Permission.GroupEducators))
              MongoDBObject("groupId" -> new ObjectId(groupId))
            else
              MongoDBObject("groupId" -> new ObjectId(groupId), "enabled" -> true)
          }
          val assignments = Assignment.find(query).toList.map(a => Json.parse(Assignment.toCompactJson(a)))
          Ok(Json.toJson(assignments))
        case None => NotFound("Group " + groupId + " not found")
      }
  }

  def create(groupId: String) = StackAction(parse.json, AuthorityKey -> NormalUser) {
    implicit request =>
      Group.findOneById(new ObjectId(groupId)) match {
        case Some(group) =>
          if (Group.hasUserPermission(group, loggedIn, Permission.GroupEducators)) {
            val title = (request.body \ "title").asOpt[String]
            val description = (request.body \ "description").asOpt[String].getOrElse {
              ""
            }
            val exercises = (request.body \ "exercises").asOpt[List[Exercise]]

            if (title.isDefined && exercises.isDefined && exercises.get.size > 0) {
              val newAssignment = Assignment(new ObjectId, title.get, description, exercises.get, group.id)
              Assignment.create(newAssignment) match {
                case Some(id) => Ok(Json.parse(Assignment.toCompactJson(newAssignment)))
                case None => UnprocessableEntity("Assignment could not be created.")
              }
            } else {
              BadRequest("Required parameters [title, exercises]")
            }
          } else Forbidden("Brak dostępu")
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

  def update(groupId: String, id: String) = StackAction(parse.json, AuthorityKey -> Educator) {
    implicit request =>
      val query = MongoDBObject("groupId" -> new ObjectId(groupId), "_id" -> new ObjectId(id))
      Assignment.findOne(query) match {
        case Some(assignment) =>
          val title = (request.body \ "title").asOpt[String]
          val description = (request.body \ "description").asOpt[String].getOrElse {
            ""
          }
          val enabled = (request.body \ "enabled").asOpt[Boolean]
          val exercises = (request.body \ "exercises").asOpt[List[Exercise]]

          if (title.isDefined && exercises.isDefined && exercises.get.size > 0 && enabled.isDefined) {
            val assignmentToUpdate = assignment.copy(title = title.get, description = description,
              exercises = exercises.get, enabled = enabled.get)
            val writeResult = Assignment.save(assignmentToUpdate)
            if (writeResult.getN > 0)
              Ok(Json.parse(Assignment.toCompactJson(assignmentToUpdate)))
            else
              UnprocessableEntity("Assignment could not be updated")
          } else {
            BadRequest("Required parameters [title, exercises, enabled]")
          }
        case None => NotFound("Assignment " + id + " not found in group " + groupId)
      }
  }

  def addProject(groupId: String, id: String) = StackAction(parse.multipartFormData, AuthorityKey -> NormalUser) {
    implicit request =>
      Group.findOneById(new ObjectId(groupId)) match {
        case Some(group) =>
          if (Group.hasUserPermission(group, loggedIn, Permission.GroupEducators)) {
            val query = MongoDBObject("groupId" -> new ObjectId(groupId), "_id" -> new ObjectId(id))
            Assignment.findOne(query) match {
              case Some(assignment) =>
                val projectFileOpt = request.body.file("projectFile")
                val testsFileOpt = request.body.file("testsFile")

                if (projectFileOpt.isDefined && testsFileOpt.isDefined) {
                  val projectFile = projectFileOpt.get
                  val testsFile = testsFileOpt.get
                  val tmpProjectFile = new File(Play.application.path + "/tmp/" + projectFile.filename)
                  val tmpTestsFile = new File(Play.application.path + "/tmp/" + testsFile.filename)

                  projectFile.ref.moveTo(tmpProjectFile, replace = true)
                  testsFile.ref.moveTo(tmpTestsFile, replace = true)

                  val projectFileIdOpt = addFile('projectFile, assignment, tmpProjectFile, projectFile.contentType.get)
                  val projectTestsFileIdOpt = addFile('projectTestsFile, assignment, tmpTestsFile, testsFile.contentType.get)

                  if (projectFileIdOpt.isDefined && projectTestsFileIdOpt.isDefined)
                    Ok(Json.obj("projectId" -> projectFileIdOpt.get.toString, "projectTestsId" -> projectTestsFileIdOpt.get.toString))
                  else
                    UnprocessableEntity("Project could not be added")
                } else
                  BadRequest("ProjectFile and ProjectTestFile must be present.")
              case None => NotFound("Assignment " + id + " not found in group " + groupId)
            }
          } else Forbidden("Brak dostępu")
        case None => NotFound("Group " + groupId + " not found")
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
              val filename = gridfsdbfile.filename.getOrElse {
                "project"
              }
              val contentType = gridfsdbfile.contentType.getOrElse {
                "text/plain"
              }
              Ok.chunked(dataContent).withHeaders(CONTENT_DISPOSITION -> ("filename=" + filename)).as(contentType)
            case None => NotFound("Not found project for assignment with id " + id)
          }
        case None => NotFound("Assignment " + id + " not found in group " + groupId)
      }
  }

  def delete(groupId: String, id: String) = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      Group.findOneById(new ObjectId(groupId)) match {
        case Some(group) =>
          if (Group.hasUserPermission(group, loggedIn, Permission.GroupEducators)) {
            val query = MongoDBObject("groupId" -> new ObjectId(groupId), "_id" -> new ObjectId(id))
            Assignment.findOne(query) match {
              case Some(assignment) =>
                if (assignment.projectId != null) Assignment.removeProject(assignment)
                if (assignment.projectTestsId != null) Assignment.removeProjectTests(assignment)
                val writeResult = Assignment.remove(assignment)
                if (writeResult.getN > 0)
                  Ok(Json.obj("message" -> ("Assignment " + id + " deleted.")))
                else
                  UnprocessableEntity("Assignment could not be deleted")
              case None => NotFound("Assignment " + id + " not found in group " + groupId)
            }
          } else Forbidden("Brak dostępu")
        case None => NotFound("Group " + groupId + " not found")
      }
  }

  private def addFile(fileType: Symbol, assignment: Assignment, file: File, contentType: String) = {
    def deleteFileBeforeResult(result: Option[ObjectId]): Option[ObjectId] = result match {
      case Some(id) => file.delete(); result
      case None => file.delete(); result
    }

    fileType match {
      case 'projectFile =>
        deleteFileBeforeResult(Assignment.addProject(assignment, file, contentType))
      case 'projectTestsFile =>
        deleteFileBeforeResult(Assignment.addProjectTests(assignment, file, contentType))
      case _ => throw new IllegalArgumentException
    }
  }

}