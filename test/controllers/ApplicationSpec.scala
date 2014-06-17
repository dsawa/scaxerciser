package controllers

import play.api.Play
import play.api.test._
import play.api.test.Helpers._
import jp.t2v.lab.play2.auth.test.Helpers._
import org.scalatest.{FunSpec, Matchers, BeforeAndAfter}
import com.github.t3hnar.bcrypt._
import com.mongodb.casbah.Imports.ObjectId
import models.Account

class ApplicationSpec extends FunSpec with Matchers with BeforeAndAfter {

  object config extends AuthConfigImpl

  lazy val usersCollection = Account.accountsCollection
  val adminId = new ObjectId

  before {
    Play.start(FakeApplication())
    val admin = new Account(adminId, "testAdmin@test.com", "qwerty".bcrypt(generateSalt), "Administrator")
    usersCollection.insert(admin.toDBObject)
  }

  after {
    usersCollection.dropCollection()
    Play.stop()
  }

  describe("Application.index") {
    it("should redirect to login page when user not logged in") {
      val result = Application.index()(FakeRequest())

      status(result) shouldEqual SEE_OTHER
      redirectLocation(result) shouldEqual Some("/login")
    }

    it("should render dashboard when user logged in") {
      val req = FakeRequest().withLoggedIn(config)(adminId)
      val result = Application.index(req)

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("text/html")
      charset(result) shouldEqual Some("utf-8")

      val content = contentAsString(result)

      content should include("testAdmin@test.com")
      content should include("Panel użytkownika")
      content should include("Panel główny")
    }
  }

  describe("Application.authenticate") {
    it("should redirect to dashboard when login data is valid") {
      val req = FakeRequest(POST, "/login").withFormUrlEncodedBody("email" -> "testAdmin@test.com", "password" -> "qwerty")
      val result = Application.authenticate(req)

      status(result) shouldEqual SEE_OTHER
      redirectLocation(result) shouldEqual Some("/")
    }

    it("should render login form when login data is invalid") {
      val req = FakeRequest(POST, "/login").withFormUrlEncodedBody("email" -> "testAdmin@test.com", "password" -> "srerty")
      val result = Application.authenticate(req)

      status(result) shouldEqual BAD_REQUEST
      contentType(result) shouldEqual Some("text/html")
      charset(result) shouldEqual Some("utf-8")

      val content = contentAsString(result)

      content should include("Błędny login lub hasło")
      content should include regex """<form.*method=\"post\".*action=\"\/login\".*""".r
      content should include regex """<input.*name=\"email\".*type=\"text\".*""".r
      content should include regex """<input.*name=\"password\".*type=\"password\".*""".r
      content should include("Zaloguj")
    }
  }

  describe("Application.login") {
    it("should render login form") {
      val req = FakeRequest(GET, "/login")
      val result = Application.login(req)

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("text/html")
      charset(result) shouldEqual Some("utf-8")

      val content = contentAsString(result)

      content should include regex """<form.*method=\"post\".*action=\"\/login\".*""".r
      content should include regex """<input.*name=\"email\".*type=\"text\".*""".r
      content should include regex """<input.*name=\"password\".*type=\"password\".*""".r
      content should include("Zaloguj")
    }
  }

  describe("Application.logout") {
    it("should redirect to the login page") {
      val req = FakeRequest(GET, "/logout")
      val result = Application.logout(req)

      status(result) shouldEqual SEE_OTHER
      redirectLocation(result) shouldEqual Some("/login")
    }
  }

}
