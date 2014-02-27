package models

import com.novus.salat.global._

//import scaxerciser.context._
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import models.relations._

case class Group(@Key("_id") id: ObjectId, name: String, accountIds: Set[ObjectId] = Set()) extends RelationalDocument {
  val db = "scaxerciser"
  val collection = "groups"
  val foreignIdsPropertyName = "accountIds"

  lazy val members = new ManyToMany[Group, Account](this, Map("toDb" -> "scaxerciser", "toCollection" -> "users"))

  def toDBObject = grater[Group].asDBObject(this)
}

object Group extends ModelCompanion[Group, ObjectId] {
  val groupsCollection = MongoConnection()("scaxerciser")("groups")
  val dao = new SalatDAO[Group, ObjectId](collection = groupsCollection) {}

  def all(): List[Group] = Group.findAll().toList

  def create(newGroup: Group): Option[ObjectId] = Group.insert(newGroup)

  def update_attributes(group: Group): WriteResult = {
    Group.update(
      q = MongoDBObject("_id" -> group.id),
      o = MongoDBObject("$set" -> MongoDBObject("name" -> group.name)),
      upsert = false, multi = false, wc = Group.dao.collection.writeConcern
    )
  }

}
