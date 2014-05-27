package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import com.mongodb.casbah.Imports.ObjectId
import ObjectIdExtension.objectIdFormat

case class GroupRole(accountId: ObjectId, permissionInGroup: String)

object GroupRole {

  implicit val groupRoleReads: Reads[GroupRole] = (
    (JsPath \ "accountId").read[ObjectId] and
      (JsPath \ "permissionInGroup").read[String]
    )(GroupRole.apply _)

}