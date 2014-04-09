package controllers

import play.api.mvc._
import com.mongodb.casbah.Imports._
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
          Account.authenticate(email, password) match {
            case Some(user) =>
              try {
                val assignmentId = request.body.dataParts("assignmentId").head
                Assignment.findOneById(new ObjectId(assignmentId)) match {
                  case Some(assignment) =>
                    val projectFile = request.body.file("projectFile").getOrElse {
                      BadRequest("File with solution is required.")
                    }

                    Ok("Solution accepted. Your results should be available in short time.")
                  case None => NotFound("Assignment " + assignmentId + " not found.")
                }
              } catch {
                case ex: NoSuchElementException => BadRequest("Param assignmentId must by present.")
              }
            case None => Unauthorized
          }
      }.getOrElse(Unauthorized)
  }

}