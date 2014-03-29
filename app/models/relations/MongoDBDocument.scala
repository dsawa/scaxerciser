package models.relations

import com.mongodb.casbah.Imports.{ObjectId, DBObject}

trait MongoDBDocument {
  val db: String
  val collection: String
  val id: ObjectId

  def toDBObject: DBObject
}
