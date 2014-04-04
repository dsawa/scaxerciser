package models.relations

import models.DBConfig
import com.mongodb.casbah.Imports._

class ManyToManyRelation[T <: ManyToMany, U <: ManyToMany](from: T, config: Map[String, String]) {
  private val mongoClient = MongoClient(DBConfig.defaultHost, DBConfig.defaultPort)
  private val fromDb = mongoClient(from.db)
  private val fromCollection = fromDb(from.collection)
  private val toDb = mongoClient(config("toDb"))
  private val toCollection = toDb(config("toCollection"))
  private lazy val toForeignIdsFieldName = lowerize(fromClassName + "Ids")

  def add(obj: U): WriteResult = {
    toCollection.update(MongoDBObject("_id" -> obj.id), $addToSet(toForeignIdsFieldName -> from.id))
    fromCollection.update(MongoDBObject("_id" -> from.id), $addToSet(from.foreignIdsPropertyName -> obj.id))
  }

  def create(obj: U): WriteResult = {
    val toDbo = obj.toDBObject
    val currObjForeignIds = objForeignIds(obj)

    toDbo(toForeignIdsFieldName) = currObjForeignIds + from.id
    toCollection.insert(toDbo)
    fromCollection.update(MongoDBObject("_id" -> from.id), $addToSet(from.foreignIdsPropertyName -> obj.id))
  }

  def all: List[DBObject] = {
    toCollection.find(MongoDBObject(toForeignIdsFieldName -> from.id)).toList
  }

  def find(query: MongoDBObject): List[DBObject] = {
    toCollection.find(query ++ MongoDBObject(toForeignIdsFieldName -> from.id)).toList
  }

  def count: Long = {
    toCollection.count(MongoDBObject(toForeignIdsFieldName -> from.id))
  }

  def remove(obj: U): WriteResult = {
    toCollection.update(MongoDBObject("_id" -> obj.id), $pull(toForeignIdsFieldName -> from.id))
    fromCollection.update(MongoDBObject("_id" -> from.id), $pull(from.foreignIdsPropertyName -> obj.id))
  }

  def removeAll(ids: Set[ObjectId]): WriteResult = {
    val newFromForeignIds = objForeignIds(from) -- ids
    val writeResult = toCollection.update(MongoDBObject("_id" -> MongoDBObject("$in" -> ids)), $pull(toForeignIdsFieldName -> from.id),
      upsert = false, multi = true)
    if (writeResult.getN > 0)
      fromCollection.update(MongoDBObject("_id" -> from.id), $set(from.foreignIdsPropertyName -> newFromForeignIds))
    else
      writeResult
  }

  def destroy(obj: U): WriteResult = {
    val query = MongoDBObject(from.foreignIdsPropertyName -> obj.id)
    val pull = $pull(from.foreignIdsPropertyName -> obj.id)
    val writeResult = toCollection.remove(obj.toDBObject)
    if (writeResult.getN > 0)
      fromCollection.update(query, pull, upsert = false, multi = true)
    else
      writeResult
  }

  private def lowerize(string: String): String = string(0).toLower + string.substring(1, string.length)

  private def fromClassName: String = from.getClass.getSimpleName

  private def objForeignIds(obj: ManyToMany): Set[ObjectId] = {
    get[ManyToMany, Set[ObjectId]](obj, obj.foreignIdsPropertyName)
  }

  private def get[Y <: ManyToMany, Z](obj: Y, property: String): Z = {
    val field = obj.getClass.getDeclaredField(property)
    field.setAccessible(true)
    field.get(obj).asInstanceOf[Z]
  }

}