package controllers

import play.api.Play
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import jp.t2v.lab.play2.auth.test.Helpers._
import org.scalatest.{FunSpec, Matchers, BeforeAndAfter}
import com.github.t3hnar.bcrypt._
import com.mongodb.casbah.Imports._
import models.{Group, Account}

import org.scalatest.mock.MockitoSugar
import org.mockito._

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

class GroupsSpec extends FunSpec with Matchers with BeforeAndAfter with MockitoSugar {

  object config extends AuthConfigImpl

  lazy val groupsCollection = Group.groupsCollection
  lazy val accountsCollection = Account.accountsCollection
  lazy val failedWriteResult: WriteResult = {
    groupsCollection.update(MongoDBObject("id" -> new ObjectId), MongoDBObject("wont" -> "update"))
  }

  val groupId_1 = new ObjectId
  val groupName_1 = "Test group"
  val groupId_2 = new ObjectId
  val groupName_2 = "Second test group"
  val adminId = new ObjectId
  val userId = new ObjectId

  before {
    Play.start(FakeApplication())
    val testGroup_1 = Group(groupId_1, groupName_1, Set(adminId))
    val testGroup_2 = Group(groupId_2, groupName_2)
    val admin = Account(adminId, "testAdmin@test.com", "qwerty".bcrypt(generateSalt), "Administrator", Set(groupId_1))
    val user = Account(userId, "testUser@test.com", "qwerty".bcrypt(generateSalt), "NormalUser")
    groupsCollection.insert(testGroup_1.toDBObject)
    groupsCollection.insert(testGroup_2.toDBObject)
    accountsCollection.insert(admin.toDBObject)
    accountsCollection.insert(user.toDBObject)
  }

  after {
    groupsCollection.dropCollection()
    accountsCollection.dropCollection()
    Play.stop()
  }

  describe("Groups.index") {
    it("should redirect to login page when user not logged in") {
      val result = Groups.index()(FakeRequest())

      status(result) shouldEqual SEE_OTHER
      redirectLocation(result) shouldEqual Some("/login")
    }

    it("should return json with groups only connected to that user") {
      val req = FakeRequest().withLoggedIn(config)(adminId)
      val result = Groups.index(req)

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("application/json")

      val content = contentAsJson(result).asInstanceOf[JsArray]
      val list = content.value.toList

      list.size shouldEqual 1
      ((list.head \ "_id") \ "$oid").as[String] shouldEqual groupId_1.toString
      (list.head \ "name").as[String] shouldEqual groupName_1
    }
  }

  describe("Groups.show") {
    it("should redirect to login page when user not logged in") {
      val result = Groups.show("itWontEvenLookForThatGroup")(FakeRequest())

      status(result) shouldEqual SEE_OTHER
      redirectLocation(result) shouldEqual Some("/login")
    }

    it("should respond with 404 when group not found") {
      val notFoundId = new ObjectId
      val req = FakeRequest(GET, "/groups/" + notFoundId.toString).withLoggedIn(config)(adminId)
      val result = Groups.show(notFoundId.toString)(req)

      status(result) shouldEqual NOT_FOUND
      contentType(result) shouldEqual Some("text/plain")

    }

    it("should return json with group data") {
      val req = FakeRequest(GET, "/groups/" + groupId_2.toString).withLoggedIn(config)(adminId)
      val result = Groups.show(groupId_2.toString)(req)

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("application/json")

      val content = contentAsJson(result).asInstanceOf[JsObject]

      ((content \ "_id") \ "$oid").as[String] shouldEqual groupId_2.toString
      (content \ "name").as[String] shouldEqual groupName_2
    }
  }

  describe("Groups.delete") {
    it("should redirect to login page when user not logged in") {
      val result = Groups.delete("itWontEvenLookForThatGroup")(FakeRequest())

      status(result) shouldEqual SEE_OTHER
      redirectLocation(result) shouldEqual Some("/login")
    }

    it("should respond with 404 when group not found") {
      val notFoundId = new ObjectId
      val req = FakeRequest(DELETE, "/groups/" + notFoundId.toString).withLoggedIn(config)(adminId)
      val result = Groups.delete(notFoundId.toString)(req)

      status(result) shouldEqual NOT_FOUND
      contentType(result) shouldEqual Some("text/plain")
    }

//      TODO: Problem z mockowaniem loggedIn itd.
//    it("should unprocessable netity") {
//      val req = FakeRequest(DELETE, "/groups/" + groupId_1.toString).withLoggedIn(config)(adminId)
//
//      val n = mock[Account]
//      val m = mock[ManyToMany[Account, Group]]
//
//      Mockito.when(m.destroy(org.mockito.Matchers.any[Group])).thenReturn(failedWriteResult)
//
//      val result = Groups.delete(groupId_1.toString)(req)
//
//      Mockito.verify(m).destroy(org.mockito.Matchers.any[Group])
//
//      status(result) shouldEqual UNPROCESSABLE_ENTITY
//      contentType(result) shouldEqual Some("text/plain")
//    }

    it("should respond with json and status OK after removing group") {
      val req = FakeRequest(DELETE, "/groups/" + groupId_1.toString).withLoggedIn(config)(adminId)
      val result = Groups.delete(groupId_1.toString)(req)

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("application/json")

      val content = contentAsJson(result).asInstanceOf[JsObject]

      (content \ "message").as[String] should include (groupId_1.toString)
    }

    it("should respond with forbidden when user is not an admin") {
      val req = FakeRequest(DELETE, "/groups/" + groupId_1.toString).withLoggedIn(config)(userId)
      val result = Groups.delete(groupId_1.toString)(req)

      status(result) shouldEqual FORBIDDEN
      contentType(result) shouldEqual Some("text/plain")
    }
  }

}
