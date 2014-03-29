package models.relations

trait ManyToMany extends MongoDBDocument {
  val foreignIdsPropertyName: String
}
