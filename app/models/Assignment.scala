package models

//import com.novus.salat.global._

import scaxerciser.context._
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import models.relations._

case class Assignment(@Key("_id") id: ObjectId, title: String, exercises: List[Exercise], groupId: ObjectId)
  extends ManyToOne {

  val db = DBConfig.assignments("db")
  val collection = DBConfig.assignments("collection")
  val foreignIdPropertyName = "groupId"

  def toDBObject = grater[Assignment].asDBObject(this)
}

object Assignment extends ModelCompanion[Assignment, ObjectId] {
  val assignmentsCollection = MongoConnection()(DBConfig.assignments("db"))(DBConfig.assignments("collection"))
  val dao = new SalatDAO[Assignment, ObjectId](collection = assignmentsCollection) {}

  def all(): List[Assignment] = Assignment.findAll().toList

  def create(assignment: Assignment): Option[ObjectId] = Assignment.insert(assignment)
}