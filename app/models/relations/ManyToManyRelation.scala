package models.relations

import models.DBConfig
import com.mongodb.casbah.Imports._

class ManyToManyRelation[T <: ManyToMany, U <: ManyToMany](from: T, config: Map[String, String]) {
  private val mongoClient = MongoClient(DBConfig.defaultHost, DBConfig.defaultPort)
  private val fromDb = mongoClient(from.db)
  private val fromCollection = fromDb(from.collection)
  private val toDb = mongoClient(config("toDb"))
  private val toCollection = toDb(config("toCollection"))
  private lazy val fromForeignIdsFieldName = config("foreignIdsField")
  private lazy val toForeignIdsFieldName = lowerize(fromClassName + "Ids")

  def add(obj: U): WriteResult = {
    toCollection.update(MongoDBObject("_id" -> obj.id), $addToSet(toForeignIdsFieldName -> from.id))
    fromCollection.update(MongoDBObject("_id" -> from.id), $addToSet(fromForeignIdsFieldName -> obj.id))
  }

  def create(obj: U, objForeignIdsField: String): WriteResult = {
    val toDbo = obj.toDBObject
    val currObjForeignIds = objForeignIds(obj, objForeignIdsField)

    toDbo(toForeignIdsFieldName) = currObjForeignIds + from.id
    toCollection.insert(toDbo)
    fromCollection.update(MongoDBObject("_id" -> from.id), $addToSet(fromForeignIdsFieldName -> obj.id))
  }

  def all: List[DBObject] = {
    toCollection.find(MongoDBObject(toForeignIdsFieldName -> from.id)).toList
  }

  def find(query: MongoDBObject, skip: Int = 0, limit: Int = 1000, sort: DBObject = MongoDBObject("_id" -> 1)): List[DBObject] = {
    toCollection.find(query ++ MongoDBObject(toForeignIdsFieldName -> from.id)).sort(sort).skip(skip).limit(limit).toList
  }

  def count: Long = {
    toCollection.count(MongoDBObject(toForeignIdsFieldName -> from.id))
  }

  def remove(obj: U): WriteResult = {
    toCollection.update(MongoDBObject("_id" -> obj.id), $pull(toForeignIdsFieldName -> from.id))
    fromCollection.update(MongoDBObject("_id" -> from.id), $pull(fromForeignIdsFieldName -> obj.id))
  }

  def removeAll(ids: Set[ObjectId]): WriteResult = {
    val newFromForeignIds = objForeignIds(from, fromForeignIdsFieldName) -- ids
    val writeResult = toCollection.update(MongoDBObject("_id" -> MongoDBObject("$in" -> ids)), $pull(toForeignIdsFieldName -> from.id),
      upsert = false, multi = true)
    if (writeResult.getN > 0)
      fromCollection.update(MongoDBObject("_id" -> from.id), $set(fromForeignIdsFieldName -> newFromForeignIds))
    else
      writeResult
  }

  def destroy(obj: U): WriteResult = {
    val query = MongoDBObject(fromForeignIdsFieldName -> obj.id)
    val pull = $pull(fromForeignIdsFieldName -> obj.id)
    val writeResult = toCollection.remove(obj.toDBObject)
    if (writeResult.getN > 0)
      fromCollection.update(query, pull, upsert = false, multi = true)
    else
      writeResult
  }

  private def lowerize(string: String): String = string(0).toLower + string.substring(1, string.length)

  private def fromClassName: String = from.getClass.getSimpleName

  private def objForeignIds(obj: ManyToMany, foreignIdsFieldName: String): Set[ObjectId] = {
    get[ManyToMany, Set[ObjectId]](obj, foreignIdsFieldName)
  }

  private def get[Y <: ManyToMany, Z](obj: Y, property: String): Z = {
    val field = obj.getClass.getDeclaredField(property)
    field.setAccessible(true)
    field.get(obj).asInstanceOf[Z]
  }

}