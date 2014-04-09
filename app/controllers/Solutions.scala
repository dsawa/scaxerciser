package controllers

import java.io.File
import play.api.Play
import play.api.Play.current
import play.api.mvc._
import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global
import com.mongodb.casbah.Imports.ObjectId
import models._

object Solutions extends Controller {

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
          val user = Account.authenticate(email, password).getOrElse(Unauthorized)
          try {
            val assignmentId = request.body.dataParts("assignmentId").head
            val assignment = Assignment.findOneById(new ObjectId(assignmentId)).getOrElse {
              NotFound("Assignment " + assignmentId + " not found.")
            }
            val projectFileOpt = request.body.file("projectFile")

            if (projectFileOpt.isDefined) {
              val tmpProjectFile = new File(Play.application.path + "/tmp/" + projectFileOpt.get.filename)
              projectFileOpt.get.ref.moveTo(tmpProjectFile, replace = true)

              Solution.create(assignment.asInstanceOf[Assignment], user.asInstanceOf[Account], tmpProjectFile) match {
                case Some(objectId) =>
                  Future {
                    Solution.analyze(objectId)
                  }
                  tmpProjectFile.delete()
                  Ok("Solution accepted. Your results should be available in short time.")
                case None =>
                  tmpProjectFile.delete()
                  UnprocessableEntity("There was problem with adding solution.")
              }

            } else BadRequest("File with solution is required.")
          } catch {
            case ex: NoSuchElementException => BadRequest("Param assignmentId must by present.")
          }
      }.getOrElse(Unauthorized)
  }

}