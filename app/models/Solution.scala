package models

//import com.novus.salat.global._

import java.io.File
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._
import com.novus.salat.annotations._
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import scaxerciser.context._

case class Solution(@Key("_id") id: ObjectId, assignmentId: ObjectId, userId: ObjectId, solutionFileId: ObjectId)

object Solution extends ModelCompanion[Solution, ObjectId] {
  val solutionsCollection = MongoConnection()(DBConfig.solutions("db"))(DBConfig.solutions("collection"))
  val dao = new SalatDAO[Solution, ObjectId](collection = solutionsCollection) {}
  val gridfs = GridFS(solutionFilesDb)

  private lazy val solutionFilesDb = MongoClient(DBConfig.defaultHost, DBConfig.defaultPort)(DBConfig.solutionsProjects("db"))

  def all(): List[Solution] = Solution.findAll().toList

  def create(assignment: Assignment, user: Account, file: File): Option[ObjectId] = {
    insertFile(file, contentType = "application/java-archive") match {
      case Some(potentialId) =>
        val solutionFileId = potentialId.asInstanceOf[ObjectId]
        dao.insert(Solution(new ObjectId, assignment.id, user.id, solutionFileId))
      case None => None
    }
  }

  // TODO: Wysle request do appki zajmujacej sie testowaniem i zapisaniem wynikow
  def analyze(id: ObjectId) {
    Thread.sleep(5000)
    println("Przetestowano " + id.toString)
  }

  private def insertFile(file: File, contentType: String): Option[AnyRef] = {
    gridfs(file) {
      f =>
        f.filename = file.getName
        f.contentType = contentType
    }
  }

}