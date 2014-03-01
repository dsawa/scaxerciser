package models.relations

import com.mongodb.casbah.Imports._

class ManyToMany[T <: RelationalDocument, U <: RelationalDocument](from: T, config: Map[String, String]) {
  private val mongoClient = MongoClient("localhost", 27017)
  private val fromDb = mongoClient(from.db)
  private val fromCollection = fromDb(from.collection)
  private val toDb = mongoClient(config("toDb"))
  private val toCollection = toDb(config("toCollection"))
  private lazy val toForeignIdsFieldName = lowerize(fromClassName + "Ids")

  def add(obj: U): WriteResult = {
    toCollection.update(MongoDBObject("_id" -> obj.id), $push(toForeignIdsFieldName -> from.id))
    fromCollection.update(MongoDBObject("_id" -> from.id), $push(from.foreignIdsPropertyName -> obj.id))
  }

  def create(obj: U): WriteResult = {
    val toDbo = obj.toDBObject
    val currObjForeignIds = objForeignIds(obj)

    toDbo(toForeignIdsFieldName) = currObjForeignIds + from.id
    toCollection.insert(toDbo)
    fromCollection.update(MongoDBObject("_id" -> from.id), $push(from.foreignIdsPropertyName -> obj.id))
  }

  def all: List[DBObject] = {
    toCollection.find(MongoDBObject(toForeignIdsFieldName -> from.id)).toList
  }

  def count: Long = {
    toCollection.count(MongoDBObject(toForeignIdsFieldName -> from.id))
  }

  def remove(obj: U): WriteResult = {
    toCollection.update(MongoDBObject(toForeignIdsFieldName -> from.id), $pull(toForeignIdsFieldName -> from.id))
    fromCollection.update(MongoDBObject(from.foreignIdsPropertyName -> obj.id), $pull(from.foreignIdsPropertyName -> obj.id))
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

  private def objForeignIds(obj: RelationalDocument): Set[ObjectId] = {
    get[RelationalDocument, Set[ObjectId]](obj, obj.foreignIdsPropertyName)
  }

  private def get[Y <: RelationalDocument, Z](obj: Y, property: String): Z = {
    val field = obj.getClass.getDeclaredField(property)
    field.setAccessible(true)
    field.get(obj).asInstanceOf[Z]
  }

}