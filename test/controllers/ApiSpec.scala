package controllers

import play.api.Play
import play.api.test._
import org.scalatest.{FunSpec, Matchers, BeforeAndAfter}
import com.mongodb.casbah.Imports._
import play.api.test.Helpers._
import play.api.test.FakeApplication
import models.{Solution, Result, TestsDetails}
import scaxerciser.config.APIConfig

class ApiSpec extends FunSpec with Matchers with BeforeAndAfter {

  object config extends AuthConfigImpl

  val solutionId = new ObjectId
  val verifiedSolutionId = new ObjectId
  val badSolutionId = new ObjectId
  val token = "testToken"

  before {
    Play.start(FakeApplication())
    val solution = Solution(solutionId, new ObjectId, new ObjectId, new ObjectId)
    val testsDetails = TestsDetails(1,2,3,4,5,6,7,8,9)
    val verifiedSolution = Solution(verifiedSolutionId, new ObjectId, new ObjectId, new ObjectId, Result("logs", "logs", testsDetails))
    Solution.save(solution)
    Solution.save(verifiedSolution)
  }

  after {
    Play.stop()
    Solution.solutionsCollection.dropCollection()
  }

  describe("Api.notifyAboutResult") {
    it("should respond with Forbidden when Auth-Token is not present") {
      val req = FakeRequest(GET, routes.Api.notifyAboutResult(solutionId.toString).url)
      val result = Api.notifyAboutResult(solutionId.toString)(req)

      status(result) shouldEqual FORBIDDEN
      contentType(result) shouldEqual Some("text/plain")
      contentAsString(result) should include ("Missing authorization credentials")
    }

    it("should respond with Forbidden when Auth-Token is present but credentials are wrong") {
      val req = FakeRequest(GET, routes.Api.notifyAboutResult(solutionId.toString).url).withHeaders("Auth-Token" -> "badToken")
      val result = Api.notifyAboutResult(solutionId.toString)(req)

      status(result) shouldEqual FORBIDDEN
      contentType(result) shouldEqual Some("text/plain")
      contentAsString(result) should include ("Bad authorization credentials")
    }

    it("should respond with NotFound when there is wrong SolutionId") {
      val req = FakeRequest(GET, routes.Api.notifyAboutResult(badSolutionId.toString).url).withHeaders("Auth-Token" -> token)
      val result = Api.notifyAboutResult(badSolutionId.toString)(req)

      status(result) shouldEqual NOT_FOUND
      contentType(result) shouldEqual Some("text/plain")
      contentAsString(result) should include (badSolutionId.toString + " not found")
    }

    it("should respond with BadRequest when Solution is not yet verified when there is wrong SolutionId") {
      val req = FakeRequest(GET, routes.Api.notifyAboutResult(solutionId.toString).url).withHeaders("Auth-Token" -> token)
      val result = Api.notifyAboutResult(solutionId.toString)(req)

      status(result) shouldEqual BAD_REQUEST
      contentType(result) shouldEqual Some("text/plain")
      contentAsString(result) should include (solutionId.toString + " has not been verified")
    }

    it("should respond with Ok when Notification is send") {
      val req = FakeRequest(GET, routes.Api.notifyAboutResult(verifiedSolutionId.toString).url).withHeaders("Auth-Token" -> token)
      val result = Api.notifyAboutResult(verifiedSolutionId.toString)(req)

      status(result) shouldEqual OK
      contentType(result) shouldEqual Some("text/plain")
      contentAsString(result) should include ("Notification send")
    }
  }

}
