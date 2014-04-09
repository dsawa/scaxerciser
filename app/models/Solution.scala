package models

//import com.novus.salat.global._

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._
import com.novus.salat.annotations._
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import scaxerciser.context._

case class Solution(@Key("_id") id: ObjectId, userId: ObjectId, assignmentId: ObjectId)

object Solution extends ModelCompanion[Solution, ObjectId] {
  val solutionsCollection = MongoConnection()(DBConfig.solutions("db"))(DBConfig.solutions("collection"))
  val dao = new SalatDAO[Solution, ObjectId](collection = solutionsCollection) {}
  val gridfs = GridFS(projectsDb)

  private lazy val projectsDb = MongoClient(DBConfig.defaultHost, DBConfig.defaultPort)(DBConfig.solutionsProjects("db"))

  def all(): List[Solution] = Solution.findAll().toList

  def create(solution: Solution): Option[ObjectId] = dao.insert(solution)
}