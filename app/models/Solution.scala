package models

//import com.novus.salat.global._

import java.io.File
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._
import com.novus.salat.annotations._
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import play.api.libs.ws._
import play.api.libs.json._
import scaxerciser.context._

case class Solution(@Key("_id") id: ObjectId, assignmentId: ObjectId, userId: ObjectId, solutionFileId: ObjectId, result: Result = null) {

  def assignment: Option[Assignment] = Assignment.findOneById(assignmentId)

}

object Solution extends ModelCompanion[Solution, ObjectId] {
  val solutionsCollection = MongoConnection()(DBConfig.solutions("db"))(DBConfig.solutions("collection"))
  val dao = new SalatDAO[Solution, ObjectId](collection = solutionsCollection) {}
  val gridfs = GridFS(solutionFilesDb)
  val analyzeApiUrl = "http://localhost:3000/api"

  private lazy val solutionFilesDb = MongoClient(DBConfig.defaultHost, DBConfig.defaultPort)(DBConfig.solutionsProjects("db"))

  def all(): List[Solution] = Solution.findAll().toList

  def all(user: Account, skip: Int = 0, limit: Int = 1000, sort: DBObject = MongoDBObject("_id" -> 1)): List[Solution] = {
    dao.find(MongoDBObject("userId" -> user.id)).sort(sort).skip(skip).limit(limit).toList
  }

  def create(assignment: Assignment, user: Account, file: File): Option[ObjectId] = {
    // TODO: Można przemyśleć i dać ogranicznik jakiś. Narazie dla porządku jedno rozwiązanie usera na zadanie.
    dao.remove(MongoDBObject("assignmentId" -> assignment.id, "userId" -> user.id))

    insertFile(file, contentType = "application/java-archive") match {
      case Some(potentialId) =>
        val solutionFileId = potentialId.asInstanceOf[ObjectId]
        dao.insert(Solution(new ObjectId, assignment.id, user.id, solutionFileId))
      case None => None
    }
  }

  def analyze(solution: Solution) {
    val url = analyzeApiUrl + "/solutions/" + solution.id.toString + "/analyze"
    val params = Json.obj(
      "id" -> JsString(solution.id.toString),
      "userId" -> JsString(solution.userId.toString),
      "assignmentId" -> JsString(solution.assignmentId.toString)
    )
    WS.url(url).withRequestTimeout(10000).post(params)
  }

  def toNormalUserJson(solution: Solution): JsObject = {
    Json.obj(
      "assignmentId" -> JsString(solution.assignmentId.toString),
      "userId" -> JsString(solution.userId.toString),
      "result" -> {
        if (solution.result == null) JsNull
        else Json.obj(
          "mark" -> JsNumber(solution.result.mark),
          "testsDetails" -> Json.obj(
            "testsSucceeded" -> JsNumber(solution.result.testsDetails.testsSucceeded),
            "totalTestsCount" -> JsNumber(solution.result.testsDetails.totalTestsCount)
          )
        )
      }
    )
  }

  private def insertFile(file: File, contentType: String): Option[AnyRef] = {
    gridfs(file) {
      f =>
        f.filename = file.getName
        f.contentType = contentType
    }
  }

}