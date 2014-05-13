package models.statistics

import play.api.libs.json.{JsPath, Writes}
import play.api.libs.functional.syntax._

case class AssignmentStats(assignmentId: String, name: String, avgMark: Double, sumMark: Double)

object AssignmentStats {

  implicit val assignmentStatsWrites: Writes[AssignmentStats] = (
    (JsPath \ "assignmentId").write[String] and
      (JsPath \ "name").write[String] and
      (JsPath \ "avgMark").write[Double] and
      (JsPath \ "sumMark").write[Double]
    )(unlift(AssignmentStats.unapply))

}
