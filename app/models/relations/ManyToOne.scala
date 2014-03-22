package models.relations

import com.mongodb.casbah.Imports.DBObject

trait ManyToOne extends MongoDBDocument {
  val foreignIdPropertyName: String

  def toDBObject: DBObject
}