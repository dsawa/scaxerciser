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

class UsersSpec extends FunSpec with Matchers with BeforeAndAfter {

  object config extends AuthConfigImpl

  lazy val accountsCollection = Account.accountsCollection
  lazy val groupsCollection = Group.groupsCollection
  lazy val failedWriteResult: WriteResult = {
    accountsCollection.update(MongoDBObject("id" -> new ObjectId), MongoDBObject("wont" -> "update"))
  }

  val groupId_1 = new ObjectId
  val groupName_1 = "Test group"
  val groupId_2 = new ObjectId
  val groupName_2 = "Second test group"
  val adminId = new ObjectId
  val adminEmail = "testAdmin@test.com"
  val userId = new ObjectId
  val userEmail = "testUser@test.com"
  val userPassword = "qwerty".bcrypt(generateSalt)
  val userPermission = "NormalUser"

  before {
    Play.start(FakeApplication())
    val admin = Account(adminId, adminEmail, "qwerty".bcrypt(generateSalt), "Administrator", Set(groupId_1))
    val user = Account(userId, userEmail, userPassword, userPermission)
    val testGroup_1 = Group(groupId_1, groupName_1, adminId)
    val testGroup_2 = Group(groupId_2, groupName_2, adminId)
    accountsCollection.insert(admin.toDBObject)
    accountsCollection.insert(user.toDBObject)
    groupsCollection.insert(testGroup_1.toDBObject)
    groupsCollection.insert(testGroup_2.toDBObject)
  }

  after {
    groupsCollection.dropCollection()
    accountsCollection.dropCollection()
    Play.stop()
  }

  describe("Users.index") {
    it("should redirect to login page when user not logged in") {
      val result = Users.index()(FakeRequest(GET, routes.Users.index().url))

      status(result) shouldEqual SEE_OTHER
      redirectLocation(result) shouldEqual Some("/login")
    }

    it("should respond with forbidden when user is not an admin") {
      val result = Users.index()(FakeRequest(GET, routes.Users.index().url).withLoggedIn(config)(userId))

      status(result) shouldEqual FORBIDDEN
      contentType(result) shouldEqual Some("text/plain")
    }

    it("should return json with users list") {
      val result = Users.index()(FakeRequest(GET, routes.Users.index().url).withLoggedIn(config)(adminId))

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("application/json")

      val content = contentAsJson(result).asInstanceOf[JsArray]
      val list = content.value.toList

      list.size shouldEqual 2
      list.exists(jsVal => (jsVal \ "email").as[String] == adminEmail) shouldEqual true
      list.exists(jsVal => ((jsVal \ "_id") \ "$oid").as[String] == adminId.toString) shouldEqual true
      list.exists(jsVal => (jsVal \ "email").as[String] == userEmail) shouldEqual true
      list.exists(jsVal => ((jsVal \ "_id") \ "$oid").as[String] == userId.toString) shouldEqual true
    }

    it("should return filtered json array when param filter is given") {
      val url = routes.Users.index().url + "?filter={\"email\":\"" + adminEmail + "\"}"
      val req = FakeRequest(GET, url).withLoggedIn(config)(adminId)
      val result = Users.index()(req)

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("application/json")

      val content = contentAsJson(result).asInstanceOf[JsArray]
      val list = content.value.toList

      list.size shouldEqual 1
      list.exists(jsVal => (jsVal \ "email").as[String] == adminEmail) shouldEqual true
      list.exists(jsVal => ((jsVal \ "_id") \ "$oid").as[String] == adminId.toString) shouldEqual true
      list.exists(jsVal => (jsVal \ "email").as[String] == userEmail) shouldEqual false
      list.exists(jsVal => ((jsVal \ "_id") \ "$oid").as[String] == userId.toString) shouldEqual false
    }
  }

  describe("Users.show") {
    it("should redirect to login page when user not logged in") {
      val result = Users.show(adminId.toString)(FakeRequest(GET, routes.Users.show(adminId.toString).url))

      status(result) shouldEqual SEE_OTHER
      redirectLocation(result) shouldEqual Some("/login")
    }

    it("should respond with forbidden when user is not an admin") {
      val result = Users.show(adminId.toString)(FakeRequest(GET, routes.Users.show(adminId.toString).url).withLoggedIn(config)(userId))

      status(result) shouldEqual FORBIDDEN
      contentType(result) shouldEqual Some("text/plain")
    }

    it("should return json with 404 when user not found") {
      val notFoundId = new ObjectId
      val req = FakeRequest(GET, routes.Users.show(notFoundId.toString).url).withLoggedIn(config)(adminId)
      val result = Users.show(notFoundId.toString)(req)

      status(result) shouldEqual NOT_FOUND
      contentType(result) shouldEqual Some("text/plain")
    }

    it("should return json with user data") {
      val req = FakeRequest(GET, routes.Users.show(adminId.toString).url).withLoggedIn(config)(adminId)
      val result = Users.show(adminId.toString)(req)

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("application/json")

      val content = contentAsJson(result).asInstanceOf[JsObject]

      ((content \ "_id") \ "$oid").as[String] shouldEqual adminId.toString
      (content \ "email").as[String] shouldEqual adminEmail
    }
  }

  describe("Users.detectPermission") {
    it("should redirect to login page when user not logged in") {
      val result = Users.detectPermission()(FakeRequest(GET, routes.Users.detectPermission().url))

      status(result) shouldEqual SEE_OTHER
      redirectLocation(result) shouldEqual Some("/login")
    }

    it("should respond with json containing info about valid permission for Administrator") {
      val result = Users.detectPermission()(FakeRequest(GET, routes.Users.detectPermission().url).withLoggedIn(config)(adminId))

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("application/json")
      (contentAsJson(result) \ "name").as[String] shouldEqual "Administrator"
    }

    it("should respond with json containing info about valid permission for NormalUser") {
      val result = Users.detectPermission()(FakeRequest(GET, routes.Users.detectPermission().url).withLoggedIn(config)(userId))

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("application/json")
      (contentAsJson(result) \ "name").as[String] shouldEqual "NormalUser"
    }
  }

  describe("Users.delete") {
    it("should redirect to login page when user not logged in") {
      val req = FakeRequest(DELETE, routes.Users.delete("itWontEvenLookForThatUser").url)
      val result = Users.delete("itWontEvenLookForThatUser")(req)

      status(result) shouldEqual SEE_OTHER
      redirectLocation(result) shouldEqual Some("/login")
    }

    it("should respond with 404 when user not found") {
      val notFoundId = new ObjectId
      val req = FakeRequest(DELETE, routes.Users.delete(notFoundId.toString).url).withLoggedIn(config)(adminId)
      val result = Users.delete(notFoundId.toString)(req)

      status(result) shouldEqual NOT_FOUND
      contentType(result) shouldEqual Some("text/plain")
    }

    it("should respond with json and status OK after removing user") {
      val beforeCount = accountsCollection.count()
      val req = FakeRequest(DELETE, routes.Users.delete(userId.toString).url).withLoggedIn(config)(adminId)
      val result = Users.delete(userId.toString)(req)

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("application/json")

      val content = contentAsJson(result).asInstanceOf[JsObject]
      (content \ "message").as[String] should include(userId.toString)

      accountsCollection.count() shouldEqual (beforeCount - 1)
    }

    it("should respond with forbidden when user is not an admin") {
      val req = FakeRequest(DELETE, routes.Users.delete(adminId.toString).url).withLoggedIn(config)(userId)
      val result = Users.delete(adminId.toString)(req)

      status(result) shouldEqual FORBIDDEN
      contentType(result) shouldEqual Some("text/plain")
    }
  }

  describe("Users.create") {
    it("should redirect to login page when user not logged in") {
      val json = Json.obj(
        "email" -> JsString("newUser@email.com"),
        "password" -> JsString("qwerty"),
        "permission" -> JsString("NormalUser")
      )
      val req = FakeRequest(POST, routes.Users.create().url).withHeaders(CONTENT_TYPE -> "application/json")
      val result = Users.create()(req.withBody(json))

      status(result) shouldEqual SEE_OTHER
      redirectLocation(result) shouldEqual Some("/login")
    }

    it("should respond with forbidden when user is not an admin") {
      val json = Json.obj(
        "email" -> JsString("newUser@email.com"),
        "password" -> JsString("qwerty"),
        "permission" -> JsString("NormalUser")
      )
      val req = FakeRequest(POST, routes.Users.create().url).withHeaders(CONTENT_TYPE -> "application/json").withLoggedIn(config)(userId)
      val result = Users.create()(req.withBody(json))

      status(result) shouldEqual FORBIDDEN
      contentType(result) shouldEqual Some("text/plain")
    }

    it("should respond with bad request when there are missing important parameters") {
      val json = Json.obj(
        "email" -> JsString("newUser@email.com"),
        "permission" -> JsString("NormalUser")
      )
      val req = FakeRequest(POST, routes.Users.create().url).withHeaders(CONTENT_TYPE -> "application/json").withLoggedIn(config)(adminId)
      val result = Users.create()(req.withBody(json))

      status(result) shouldEqual BAD_REQUEST
      contentType(result) shouldEqual Some("text/plain")
    }

    it("should create user and respond with ok") {
      val beforeCount = accountsCollection.count()
      val json = Json.obj(
        "email" -> JsString("newUser@email.com"),
        "password" -> JsString("qwerty"),
        "permission" -> JsString("NormalUser")
      )
      val req = FakeRequest(POST, routes.Users.create().url).withHeaders(CONTENT_TYPE -> "application/json").withLoggedIn(config)(adminId)
      val result = Users.create()(req.withBody(json))

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("application/json")

      val content = contentAsJson(result).asInstanceOf[JsObject]
      content.keys.exists(key => key == "id") shouldEqual true

      accountsCollection.count() shouldEqual (beforeCount + 1)
    }
  }

  describe("Users.update") {
    it("should redirect to login page when user not logged in") {
      val json = Json.obj(
        "email" -> JsString("updatedUser@email.com"),
        "permission" -> JsString("Administrator")
      )
      val req = FakeRequest(PUT, routes.Users.update(userId.toString).url).withHeaders(CONTENT_TYPE -> "application/json")
      val result = Users.update(userId.toString)(req.withBody(json))

      status(result) shouldEqual SEE_OTHER
      redirectLocation(result) shouldEqual Some("/login")
    }

    it("should respond with bad request when there are missing important parameters") {
      val json = Json.obj(
        "permission" -> JsString("Administrator")
      )
      val req = FakeRequest(PUT, routes.Users.update(userId.toString).url).
        withHeaders(CONTENT_TYPE -> "application/json").withLoggedIn(config)(adminId)
      val result = Groups.update(userId.toString)(req.withBody(json))

      status(result) shouldEqual BAD_REQUEST
      contentType(result) shouldEqual Some("text/plain")
    }

    it("should respond with 404 when group is not found") {
      val notFoundId = new ObjectId
      val json = Json.obj(
        "email" -> JsString("newUser@email.com"),
        "password" -> JsString("qwerty"),
        "permission" -> JsString("NormalUser")
      )
      val req = FakeRequest(PUT, routes.Users.update(notFoundId.toString).url).
        withHeaders(CONTENT_TYPE -> "application/json").withLoggedIn(config)(adminId)
      val result = Users.update(notFoundId.toString)(req.withBody(json))

      status(result) shouldEqual NOT_FOUND
      contentType(result) shouldEqual Some("text/plain")
      contentAsString(result) should include (notFoundId.toString)
    }

    it("should update user but leave password the same when is not given in params") {
      val json = Json.obj(
        "email" -> JsString("newUser@email.com"),
        "permission" -> JsString("Administrator")
      )
      val req = FakeRequest(PUT, routes.Users.update(userId.toString).url).
        withHeaders(CONTENT_TYPE -> "application/json").withLoggedIn(config)(adminId)
      val result = Users.update(userId.toString)(req.withBody(json))

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("application/json")

      val content = contentAsJson(result)
      (content \ "email").as[String] shouldEqual "newUser@email.com"
      (content \ "permission").as[String] shouldEqual "Administrator"

      val thatUser = Account.findOneById(userId).get
      thatUser.email should not be userEmail
      thatUser.email shouldEqual "newUser@email.com"
      thatUser.permission should not be userPermission
      thatUser.permission shouldEqual "Administrator"
      thatUser.password shouldEqual userPassword
    }

    it("should update user data with password when given in params") {
      val newPassword = "newPassword"
      val json = Json.obj(
        "email" -> JsString("newUser@email.com"),
        "password" -> JsString(newPassword),
        "permission" -> JsString("Administrator")
      )
      val req = FakeRequest(PUT, routes.Users.update(userId.toString).url).
        withHeaders(CONTENT_TYPE -> "application/json").withLoggedIn(config)(adminId)
      val result = Users.update(userId.toString)(req.withBody(json))

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("application/json")

      val content = contentAsJson(result)
      (content \ "email").as[String] shouldEqual "newUser@email.com"
      newPassword.isBcrypted((content \ "password").as[String]) shouldEqual true
      (content \ "permission").as[String] shouldEqual "Administrator"

      val thatUser = Account.findOneById(userId).get
      thatUser.email should not be userEmail
      thatUser.email shouldEqual "newUser@email.com"
      thatUser.permission should not be userPermission
      thatUser.permission shouldEqual "Administrator"
      userPassword.isBcrypted(thatUser.password) shouldEqual false
      newPassword.isBcrypted(thatUser.password) shouldEqual true
    }
  }

}
