package models

import scaxerciser.context._
import com.mongodb.casbah.Imports._
import com.novus.salat.annotations._
import com.novus.salat.dao.{ SalatDAO, ModelCompanion }

case class Group(@Key("_id") id: ObjectId, name: String)

object Group extends ModelCompanion[Group, ObjectId] {
    val groupsCollection = MongoConnection()("scaxerciser")("groups")
    val dao = new SalatDAO[Group, ObjectId](collection = groupsCollection) {}

    def all(): List[Group] = Group.findAll().toList

    def create(name: String) = {
      val newGroup = new Group(new ObjectId, name)
      Group.insert(newGroup) match {
        case Some(id) => newGroup
        case None => null
      }
    }

   def edit(id: String, newName: String) = {
     val objectId = new ObjectId(id)
     Group.update(
       q = MongoDBObject("_id" -> objectId),
       o = MongoDBObject("$set" -> MongoDBObject("name" -> newName)),
       upsert = false, multi = false, wc = Group.dao.collection.writeConcern
     )
   }

    def delete(id: String) = {
      val objectId = new ObjectId(id)
      Group.findOneById(objectId) match {
        case Some(group) => Group.remove(group)
        case None => null
      }
    }
}
