package models.relations

import com.mongodb.casbah.Imports.{ObjectId, DBObject}

trait RelationalDocument {
  val id: ObjectId
  val foreignIdsPropertyName: String
  val db: String
  val collection: String

  def toDBObject: DBObject
}
