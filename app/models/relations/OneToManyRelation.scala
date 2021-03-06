package models.relations

import com.mongodb.casbah.Imports._
import scaxerciser.config.DBConfig

class OneToManyRelation[T <: OneToMany, U <: ManyToOne](from: T, config: Map[String, String]) {

  private val mongoClient = MongoClient(DBConfig.defaultHost, DBConfig.defaultPort)
  private val fromDb = mongoClient(from.db)
  private val fromCollection = fromDb(from.collection)
  private val toDb = mongoClient(config("toDb"))
  private val toCollection = toDb(config("toCollection"))
  private lazy val toForeignIdFieldName = lowerize(fromClassName + "Id")

  def create(obj: U) = {
    val toDbo = obj.toDBObject
    toDbo(toForeignIdFieldName) = from.id
    toCollection.insert(toDbo)
  }

  def all: List[DBObject] = {
    toCollection.find(MongoDBObject(toForeignIdFieldName -> from.id)).toList
  }

  def distinct[Y <: Any](field: String): List[Y] = {
    toCollection.distinct(field, MongoDBObject(toForeignIdFieldName -> from.id)).toList.map(r => r.asInstanceOf[Y])
  }

  def destroyAll: WriteResult = {
    toCollection.remove(MongoDBObject(toForeignIdFieldName -> from.id))
  }

  private def lowerize(string: String): String = string(0).toLower + string.substring(1, string.length)

  private def fromClassName: String = from.getClass.getSimpleName

}