package controllers

import java.io.File
import play.api.Play
import play.api.Play.current
import play.api.mvc._
import play.api.libs.json._
import com.mongodb.casbah.Imports._
import jp.t2v.lab.play2.auth.AuthElement
import models._

object Solutions extends Controller with AuthElement with AuthConfigImpl {

  def submit(id: String) = Action(parse.multipartFormData) {
    implicit request =>
      def decodeBasicAuth(auth: String) = {
        val baStr = auth.replaceFirst("Basic ", "")
        val Array(user, pass) = new String(new sun.misc.BASE64Decoder().decodeBuffer(baStr), "UTF-8").split(":")
        (user, pass)
      }

      request.headers.get("Authorization").map {
        basicAuth =>
          val (email, password) = decodeBasicAuth(basicAuth)
          Account.authenticate(email, password) match {
            case Some(user) =>
              try {
                val assignmentId = request.body.dataParts("assignmentId").head
                Assignment.findOneById(new ObjectId(assignmentId)) match {
                  case Some(assignment) =>
                    val projectFileOpt = request.body.file("solutionFile")

                    if (projectFileOpt.isDefined) {
                      val tmpProjectFile = new File(Play.application.path + "/tmp/" + projectFileOpt.get.filename)
                      projectFileOpt.get.ref.moveTo(tmpProjectFile, replace = true)

                      Solution.create(assignment, user, tmpProjectFile) match {
                        case Some(objectId) =>
                          SolutionSender.sendToAnalyze(objectId)
                          tmpProjectFile.delete()
                          Ok("Solution accepted. Your results should be available in short time.")
                        case None =>
                          tmpProjectFile.delete()
                          UnprocessableEntity("There was problem with adding solution.")
                      }

                    } else BadRequest("File with solution is required.")

                  case None => NotFound("Assignment " + assignmentId + " not found.")
                }
              } catch {
                case ex: NoSuchElementException => BadRequest("Param assignmentId must by present.")
              }
            case None => Unauthorized
          }
      }.getOrElse(Unauthorized)
  }

  def show(assignmentId: String, id: String) = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      val currentUser = loggedIn
      val query = MongoDBObject("assignmentId" -> new ObjectId(assignmentId), "_id" -> new ObjectId(id), "userId" -> currentUser.id)
      Solution.findOne(query) match {
        case Some(solution) => Ok(Json.parse(Solution.toCompactJson(solution)))
        case None => NotFound("Solution " + id + " not found.")
      }
  }

  def assignmentSolutions(assignmentId: String) = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      val currentUser = loggedIn
      val query = MongoDBObject("assignmentId" -> new ObjectId(assignmentId), "userId" -> currentUser.id)
      Solution.findOne(query) match {
        case Some(solution) => Ok(Solution.toNormalUserJson(solution))
        case None => NotFound("Solution for assignment " + assignmentId + " and user " + currentUser.id.toString + "not found.")
      }
  }

  def userSolution(userId: String, id: String) = StackAction(AuthorityKey -> Educator) {
    implicit request =>
      val query = MongoDBObject("userId" -> new ObjectId(userId), "_id" -> new ObjectId(id))
      Solution.findOne(query) match {
        case Some(solution) => Ok(Json.parse(Solution.toCompactJson(solution)))
        case None => NotFound("Solution " + id + " not found.")
      }
  }

  def userSolutions(userId: String) = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      Account.findOneById(new ObjectId(userId)) match {
        case Some(user) =>
          if (currentUserHasAccess(loggedIn, userId)) {
            val lastSolutions = Solution.all(user, sort = MongoDBObject("_id" -> -1))
            Ok(Solution.toNormalUserArrayJson(lastSolutions))
          } else
            BadRequest("Brak dostępu")
        case None => NotFound("User " + userId + " not found.")
      }
  }

  def groupMemberSolutions(groupId: String, userId: String) = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      Group.findOneById(new ObjectId(groupId)) match {
        case Some(group) =>
          if (Group.hasUserPermission(group, loggedIn, Permission.GroupEducators)) {
            val assignmentIds = group.assignments.distinct[ObjectId]("_id")
            val query = MongoDBObject("userId" -> new ObjectId(userId), "assignmentId" -> MongoDBObject("$in" -> assignmentIds))
            val lastSolutions = Solution.find(query).sort(MongoDBObject("_id" -> -1)).toList
            Ok(Solution.toNormalUserArrayJson(lastSolutions))
          } else Forbidden("Brak dostępu")
        case None => NotFound("Group " + groupId + " not found.")
      }
  }

  private def currentUserHasAccess(currentUser: Account, paramId: String): Boolean =
    Account.isEducator(currentUser) || Account.isAdmin(currentUser) || currentUser.id.toString == paramId

}