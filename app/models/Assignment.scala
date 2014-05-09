package models

//import com.novus.salat.global._

import java.io.File
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import scaxerciser.context._
import models.relations._

case class Assignment(@Key("_id") id: ObjectId, title: String = "", description: String, exercises: List[Exercise],
                      groupId: ObjectId, projectId: ObjectId = null, projectTestsId: ObjectId = null, enabled: Boolean = false)
  extends ManyToOne {

  val db = DBConfig.assignments("db")
  val collection = DBConfig.assignments("collection")

  def toDBObject = grater[Assignment].asDBObject(this)
}

object Assignment extends ModelCompanion[Assignment, ObjectId] {
  val assignmentsCollection = MongoConnection()(DBConfig.assignments("db"))(DBConfig.assignments("collection"))
  val dao = new SalatDAO[Assignment, ObjectId](collection = assignmentsCollection) {}
  val gridfs = GridFS(projectsDb)

  private lazy val projectsDb = MongoClient(DBConfig.defaultHost, DBConfig.defaultPort)(DBConfig.assignmentsProjects("db"))

  def all(): List[Assignment] = Assignment.findAll().toList

  def findByGroupId(groupId: ObjectId): List[Assignment] = {
    dao.find(MongoDBObject("groupId" -> groupId)).toList
  }

  def create(assignment: Assignment): Option[ObjectId] = dao.insert(assignment)

  def addProject(assignment: Assignment, file: File, contentType: String = "application/zip"): Option[ObjectId] = {
    if (assignment.projectId != null) removeProject(assignment)

    addFile(file, contentType) match {
      case Some(potentialId) =>
        val projectId = potentialId.asInstanceOf[ObjectId]
        val writeResult = dao.update(MongoDBObject("_id" -> assignment.id), $set("projectId" -> projectId))
        if (writeResult.getN > 0) Some(projectId)
        else {
          gridfs.remove(projectId)
          None
        }
      case None => None
    }
  }

  def addProjectTests(assignment: Assignment, file: File, contentType: String = "application/zip"): Option[ObjectId] = {
    if (assignment.projectTestsId != null) removeProjectTests(assignment)

    addFile(file, contentType) match {
      case Some(potentialId) =>
        val projectTestsId = potentialId.asInstanceOf[ObjectId]
        val writeResult = dao.update(MongoDBObject("_id" -> assignment.id), $set("projectTestsId" -> projectTestsId))
        if (writeResult.getN > 0) Some(projectTestsId)
        else {
          gridfs.remove(projectTestsId)
          None
        }
      case None => None
    }
  }

  def getProject(assignment: Assignment): Option[GridFSDBFile] = {
    gridfs.findOne(assignment.projectId)
  }

  def removeProject(assignment: Assignment) {
    gridfs.remove(assignment.projectId)
  }

  def removeProjectTests(assignment: Assignment) {
    gridfs.remove(assignment.projectTestsId)
  }

  def removeAllProjectsFromGroup(groupId: ObjectId) {
    val projectIds = findByGroupId(groupId).map(assignment => {
      assignment.projectId
    })
    projectIds.foreach(projectId => gridfs.remove(projectId))
  }

  private def addFile(file: File, contentType: String): Option[AnyRef] = {
    gridfs(file) {
      f =>
        f.filename = file.getName
        f.contentType = contentType
    }
  }
}