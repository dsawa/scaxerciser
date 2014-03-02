package controllers

import play.api.mvc.Controller
import play.api.libs.json._
import com.github.t3hnar.bcrypt._
import com.mongodb.casbah.Imports.ObjectId
import jp.t2v.lab.play2.auth.AuthElement
import models._

object Users extends Controller with AuthElement with AuthConfigImpl {

  private def currentUserHasAccess(currentUser: Account, paramId: String): Boolean =
    Permission.valueOf(currentUser.permission) == Administrator || currentUser.id.toString == paramId

  def index = StackAction(AuthorityKey -> Administrator) {
    implicit request =>
      val users = Account.all().map(u => Json.parse(Account.toCompactJson(u)))
      Ok(Json.toJson(users))
  }

  def create = StackAction(parse.json, AuthorityKey -> Administrator) {
    implicit request =>
      val email = (request.body \ "email").asOpt[String]
      val password = (request.body \ "password").asOpt[String]
      val permission = (request.body \ "permission").asOpt[String]
      if (email.isDefined && password.isDefined && permission.isDefined) {
        Permission.valueOf(permission.get)
        val newAccount = new Account(new ObjectId, email.get, password.get.bcrypt(generateSalt), permission.get)
        Account.create(newAccount) match {
          case Some(id) => Ok(Json.obj("id" -> id.toString))
          case None => UnprocessableEntity("Group could not be created.")
        }
      } else {
        BadRequest("Missing parameter. Required parameters [email, password, permission]")
      }
  }

  def show(id: String) = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      if (currentUserHasAccess(loggedIn, id)) {
        Account.findOneById(new ObjectId(id)) match {
          case Some(account) => Ok(Json.parse(Account.toCompactJson(account)))
          case None => NotFound("User " + id + " not found")
        }
      } else {
        Forbidden("Brak dostępu")
      }
  }

  def update(id: String) = StackAction(parse.json, AuthorityKey -> NormalUser) {
    implicit request =>
      if (currentUserHasAccess(loggedIn, id)) {
        Account.findOneById(new ObjectId(id)) match {
          case Some(account) =>
            val newEmail = (request.body \ "email").asOpt[String]
            val newPassword = (request.body \ "password").asOpt[String]
            val newPermission = (request.body \ "permission").asOpt[String]
            if (newEmail.isDefined && newPermission.isDefined) {
              val password = if (newPassword.isDefined) newPassword.get.bcrypt(generateSalt) else account.password
              val toUpdate = account.copy(email = newEmail.get, password = password, permission = newPermission.get)
              val writeResult = Account.updateAttributes(toUpdate)
              if (writeResult.getN > 0)
                Ok(Json.parse(Account.toCompactJson(toUpdate)))
              else
                UnprocessableEntity("User " + id + "could not be updated.")
            } else {
              BadRequest("Missing parameter. Required parameters [email, permission]")
            }
          case None => NotFound("User " + id + " not found")
        }
      } else {
        Forbidden("Brak dostępu")
      }
  }

  def delete(id: String) = StackAction(AuthorityKey -> Administrator) {
    implicit request =>
      Account.findOneById(new ObjectId(id)) match {
        case Some(account) =>
          val writeResult = Account.destroy(account)
          if (writeResult.getN > 0)
            Ok(Json.obj("message" -> ("User " + id + " deleted.")))
          else
            UnprocessableEntity("User " + id + " could not be deleted.")
        case None => NotFound("User " + id + " not found")
      }
  }

  def detectPermission = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      val currentUser: User = loggedIn
      Ok(Json.obj("name" -> currentUser.permission))
  }

}