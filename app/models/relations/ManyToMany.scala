package models.relations

import com.mongodb.casbah.Imports._

class ManyToMany[T <: RelationalDocument, U <: RelationalDocument](from: T, config: Map[String, String]) {
  private val mongoClient = MongoClient("localhost", 27017)
  private val fromDb = mongoClient(from.db)
  private val fromCollection = fromDb(from.collection)
  private val toDb = mongoClient(config("toDb"))
  private val toCollection = toDb(config("toCollection"))
  private lazy val toForeignIdsFieldName = lowerize(fromClassName + "Ids")

  def create(obj: U): WriteResult = {
    val toDbo = obj.toDBObject
    val currObjForeignIds = objForeignIds(obj)
    val currFromForeignIds = objForeignIds(from)

    toDbo(toForeignIdsFieldName) = currObjForeignIds + from.id
    toCollection.insert(toDbo)
    fromCollection.update(MongoDBObject("_id" -> from.id), $set(from.foreignIdsPropertyName -> (currFromForeignIds + obj.id)))
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