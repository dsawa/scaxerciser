package controllers

import play.api.Play
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import jp.t2v.lab.play2.auth.test.Helpers._
import org.scalatest.{FunSpec, Matchers, BeforeAndAfter}
import com.github.t3hnar.bcrypt._
import com.mongodb.casbah.Imports._
import models.{Account, Group, Assignment, Exercise}

class AssignmentsSpec extends FunSpec with Matchers with BeforeAndAfter {

  object config extends AuthConfigImpl

  lazy val assignmentsCollection = Assignment.assignmentsCollection
  lazy val groupsCollection = Group.groupsCollection
  lazy val accountsCollection = Account.accountsCollection
  lazy val failedWriteResult: WriteResult = {
    assignmentsCollection.update(MongoDBObject("id" -> new ObjectId), MongoDBObject("wont" -> "update"))
  }

  val adminId = new ObjectId
  val adminEmail = "testAdmin@test.com"
  val userId = new ObjectId
  val userEmail = "testUser@test.com"
  val userPassword = "qwerty".bcrypt(generateSalt)
  val userPermission = "NormalUser"
  val groupId_1 = new ObjectId
  val groupName_1 = "Test group"
  val groupId_2 = new ObjectId
  val groupName_2 = "Second test group"
  val assignmentId = new ObjectId
  val assignmentTitle = "Recursion"
  val assignmentDescription = "You need to understand recursion to do this"
  val assignmentExercises = List(Exercise("Do function with tail recursion"))

  before {
    Play.start(FakeApplication())
    val admin = Account(adminId, adminEmail, "qwerty".bcrypt(generateSalt), "Administrator", Set(groupId_1))
    val user = Account(userId, userEmail, userPassword, userPermission)
    val testGroup_1 = Group(groupId_1, groupName_1, adminId)
    val testGroup_2 = Group(groupId_2, groupName_2, adminId)
    val assignment = Assignment(assignmentId, assignmentTitle, assignmentDescription, assignmentExercises, groupId_1)
    accountsCollection.insert(admin.toDBObject)
    accountsCollection.insert(user.toDBObject)
    assignmentsCollection.insert(assignment.toDBObject)
    groupsCollection.insert(testGroup_1.toDBObject)
    groupsCollection.insert(testGroup_2.toDBObject)
  }

  after {
    groupsCollection.dropCollection()
    assignmentsCollection.dropCollection()
    accountsCollection.dropCollection()
    Play.stop()
  }

  describe("Assignments.index") {
    it("should redirect to login page when user not logged in") {
      val result = Assignments.index(groupId_1.toString)(FakeRequest())

      status(result) shouldEqual SEE_OTHER
      redirectLocation(result) shouldEqual Some("/login")
    }

    it("should return json with assignments connected to group with given id") {
      val req = FakeRequest(GET, routes.Assignments.index(groupId_1.toString).url).withLoggedIn(config)(adminId)
      val result = Assignments.index(groupId_1.toString)(req)

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("application/json")

      val content = contentAsJson(result).asInstanceOf[JsArray]
      val list = content.value.toList

      list.size shouldEqual 1
      ((list.head \ "_id") \ "$oid").as[String] shouldEqual assignmentId.toString
      ((list.head \ "groupId") \ "$oid").as[String] shouldEqual groupId_1.toString
      (list.head \ "title").as[String] shouldEqual assignmentTitle
    }
  }

  describe("Assignments.create") {
    it("should redirect to login page when user not logged in") {
      val json = Json.obj(
        "title" -> JsString("Calculator"),
        "exercises" -> Json.arr(
          Json.obj("description" -> JsString("make calculator"))
        )
      )
      val req = FakeRequest(POST, routes.Assignments.create(groupId_1.toString).url).
        withHeaders(CONTENT_TYPE -> "application/json")
      val result = Assignments.create(groupId_1.toString)(req.withBody(json))

      status(result) shouldEqual SEE_OTHER
      redirectLocation(result) shouldEqual Some("/login")
    }

    it("should respond with forbidden when user is not an admin") {
      val json = Json.obj(
        "title" -> JsString("Calculator"),
        "exercises" -> Json.arr(
          Json.obj("description" -> JsString("make calculator"))
        )
      )
      val req = FakeRequest(POST, routes.Assignments.create(groupId_1.toString).url).
        withHeaders(CONTENT_TYPE -> "application/json").withLoggedIn(config)(userId)
      val result = Assignments.create(groupId_1.toString)(req.withBody(json))

      status(result) shouldEqual FORBIDDEN
      contentType(result) shouldEqual Some("text/plain")
    }

    it("should respond with bad request when there are missing important parameters") {
      val json = Json.obj(
        "title" -> JsString("Calculator")
      )
      val req = FakeRequest(POST, routes.Assignments.create(groupId_1.toString).url).
        withHeaders(CONTENT_TYPE -> "application/json").withLoggedIn(config)(adminId)
      val result = Assignments.create(groupId_1.toString)(req.withBody(json))

      status(result) shouldEqual BAD_REQUEST
      contentType(result) shouldEqual Some("text/plain")
    }

    it("should create assignment and respond with ok") {
      val beforeCount = assignmentsCollection.count()
      val json = Json.obj(
        "title" -> JsString("Calculator"),
        "exercises" -> Json.arr(
          Json.obj("description" -> JsString("make calculator"), "hint" -> JsString("a"))
        )
      )
      val req = FakeRequest(POST, routes.Assignments.create(groupId_1.toString).url).
        withHeaders(CONTENT_TYPE -> "application/json").withLoggedIn(config)(adminId)
      val result = Assignments.create(groupId_1.toString)(req.withBody(json))

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("application/json")

      val content = contentAsJson(result)
      (content \ "title").as[String] shouldEqual "Calculator"

      assignmentsCollection.count() shouldEqual (beforeCount + 1)
    }
  }

}
