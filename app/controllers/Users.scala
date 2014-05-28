package controllers

import play.api.mvc.Controller
import play.api.libs.json._
import com.github.t3hnar.bcrypt._
import com.mongodb.casbah.Imports._
import jp.t2v.lab.play2.auth.AuthElement
import models._
import ObjectIdExtension.objectIdFormat

object Users extends Controller with AuthElement with AuthConfigImpl {

  private def currentUserHasAccess(currentUser: Account, paramId: String): Boolean =
    Account.isEducator(currentUser) || Account.isAdmin(currentUser) || currentUser.id.toString == paramId

  def index = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      val params = request.queryString.map { case (k, v) => k -> v.mkString}
      val currentUser: User = loggedIn
      val users = {
        if (params.contains("filter")) {
          val query = com.mongodb.util.JSON.parse(params("filter")).asInstanceOf[DBObject]
          Account.find(query).toList.map(u => Json.parse(Account.toCompactJson(u)))
        } else if (Account.isEducator(currentUser)) {
          val query = MongoDBObject("groupIds" -> MongoDBObject("$in" -> currentUser.groupIds))
          Account.find(query).toList.map(u => Json.parse(Account.toCompactJson(u)))
        } else {
          Account.all().map(u => Json.parse(Account.toCompactJson(u)))
        }
      }
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
      Ok(Json.obj("name" -> currentUser.permission, "accountId" -> currentUser.id))
  }

  def groupMembers(groupId: String) = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      Group.findOneById(new ObjectId(groupId)) match {
        case Some(group) =>
          val params = request.queryString.map { case (k, v) => k -> v.mkString}
          if (params.contains("filter")) {
            val query = com.mongodb.util.JSON.parse(params("filter")).asInstanceOf[DBObject]
            val members = group.members.find(query).map(dbo => Account.toObject(dbo))
            Ok(Account.toCompactJSONArray(members)).withHeaders("content-type" -> "application/json")
          } else {
            val members = group.members.all.map(dbo => Account.toObject(dbo))
            Ok(Account.toCompactJSONArray(members)).withHeaders("content-type" -> "application/json")
          }
        case None => NotFound("Group " + groupId + " not found.")
      }
  }

  def groupEducators(groupId: String) = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      Group.findOneById(new ObjectId(groupId)) match {
        case Some(group) =>
          if (Group.hasUserPermission(group, loggedIn, Permission.GroupEducators)) {
            val groupEducatorsIds = group.groupRoles.filter(gr => {
              gr.roleInGroup == Educator.toString || gr.roleInGroup == Administrator.toString
            }).map(gr => gr.accountId)
            val educators = group.members.find(MongoDBObject("_id" -> MongoDBObject("$in" -> groupEducatorsIds))).map(dbo => Account.toObject(dbo))
            Ok(Account.toCompactJSONArray(educators)).withHeaders("content-type" -> "application/json")
          } else Forbidden("Brak dostępu")
        case None => NotFound("Group " + groupId + " not found.")
      }
  }

  def groupNormalUsers(groupId: String) = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      Group.findOneById(new ObjectId(groupId)) match {
        case Some(group) =>
          if (Group.hasUserPermission(group, loggedIn, Permission.GroupEducators)) {
            val groupEducatorsIds = group.groupRoles.filter(gr => gr.roleInGroup == NormalUser.toString).map(gr => gr.accountId)
            val educators = group.members.find(MongoDBObject("_id" -> MongoDBObject("$in" -> groupEducatorsIds))).map(dbo => Account.toObject(dbo))
            Ok(Account.toCompactJSONArray(educators)).withHeaders("content-type" -> "application/json")
          } else Forbidden("Brak dostępu")
        case None => NotFound("Group " + groupId + " not found.")
      }
  }

  def addUserToGroup(groupId: String, id: String) = StackAction(parse.json, AuthorityKey -> NormalUser) {
    implicit request =>
      (request.body \ "roleInGroup").asOpt[String] match {
        case Some(roleInGroupParam) =>
          val roleInGroup = Permission.valueOf(roleInGroupParam).toString
          Group.findOneById(new ObjectId(groupId)) match {
            case Some(group) =>
              if (Group.hasUserPermission(group, loggedIn, Permission.GroupEducators)) {
                Account.findOneById(new ObjectId(id)) match {
                  case Some(user) =>
                    val newGroupRoles = group.groupRoles.filter(gr => gr.accountId != user.id) + GroupRole(user.id, roleInGroup)
                    val updatedGroup = group.copy(accountIds = group.accountIds + user.id, groupRoles = newGroupRoles)
                    val updatedAccount = user.copy(groupIds = user.groupIds + group.id)

                    if (Group.save(updatedGroup).getN > 0 && Account.save(updatedAccount).getN > 0)
                      Ok(Json.parse(Account.toCompactJson(updatedAccount)))
                    else UnprocessableEntity("User " + id + " could not be added to group " + groupId)
                  case None => NotFound("User " + id + " not found.")
                }
              } else Forbidden("Brak dostępu")
            case None => NotFound("Group " + groupId + " not found.")
          }
        case None => BadRequest("Specify roleInGroup for user.")
      }
  }

  def removeUserFromGroup(groupId: String, id: String) = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      Group.findOneById(new ObjectId(groupId)) match {
        case Some(group) =>
          if (Group.hasUserPermission(group, loggedIn, Permission.GroupEducators)) {
            Account.findOneById(new ObjectId(id)) match {
              case Some(user) =>
                val newGroupRoles = group.groupRoles.filter(gr => gr.accountId != user.id)
                val updatedGroup = group.copy(accountIds = group.accountIds - user.id, groupRoles = newGroupRoles)
                val updatedAccount = user.copy(groupIds = user.groupIds - group.id)

                if (Group.save(updatedGroup).getN > 0 && Account.save(updatedAccount).getN > 0)
                  Ok(Json.parse(Account.toCompactJson(updatedAccount)))
                else UnprocessableEntity("User " + id + " could not be removed to group " + groupId)
              case None => NotFound("User " + id + " not found.")
            }
          } else Forbidden("Brak dostępu")
        case None => NotFound("Group " + groupId + " not found.")
      }
  }
}