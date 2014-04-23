package models

import play.api.libs.json._

case class TestsDetails(testsExpected: Int, testsSucceeded: Int, testsFailed: Int, testsIgnored: Int,
                        testsCanceled: Int, testsCompleted: Int, totalTestsCount: Int, suitesCompleted: Int,
                        suitesAborted: Int, error: String = null)

case class Result(processLogs: String, testsLogs: String, testsDetails: TestsDetails, mark: Double = 0)

object Result {

  def calculateMark(result: Result): Result = {
    val newMark = (result.testsDetails.totalTestsCount.toDouble / result.testsDetails.testsSucceeded) * 100

    result.copy(mark = newMark)
  }

  def testsDetailsFromJson(json: JsValue): TestsDetails = {
    def hasErrors: Boolean = (json \ "error").asOpt[JsValue].isDefined

    val error = {
      if (hasErrors) {
        (json \ "error").asOpt[JsValue].get.asInstanceOf[JsObject].values.foldLeft[String]("")(
          (first, second) => first + ", " + second)
      } else null
    }

    new TestsDetails(
      (json \ "testsExpected").asOpt[Int].getOrElse(0),
      (json \ "testsSucceeded").asOpt[Int].getOrElse(0),
      (json \ "testsFailed").asOpt[Int].getOrElse(0),
      (json \ "testsIgnored").asOpt[Int].getOrElse(0),
      (json \ "testsCanceled").asOpt[Int].getOrElse(0),
      (json \ "testsCompleted").asOpt[Int].getOrElse(0),
      (json \ "totalTestsCount").asOpt[Int].getOrElse(0),
      (json \ "suitesCompleted").asOpt[Int].getOrElse(0),
      (json \ "suitesAborted").asOpt[Int].getOrElse(0),
      error
    )
  }

}

