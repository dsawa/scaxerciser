package controllers

import play.api.mvc._
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
            case Some(user) => Ok(s"$email identified by $password")
            case None => Unauthorized
          }
      }.getOrElse(Unauthorized)
  }

}