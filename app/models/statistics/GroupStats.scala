package models.statistics

import play.api.libs.json.{JsPath, Writes}
import play.api.libs.functional.syntax._

case class GroupStats(name: String, membersCount: Long, normalUsersCount: Long, educatorsCount: Long, assignmentsCount: Long)

object GroupStats {

  implicit val groupStatsWrites: Writes[GroupStats] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "membersCount").write[Long] and
      (JsPath \ "normalUsersCount").write[Long] and
      (JsPath \ "educatorsCount").write[Long] and
      (JsPath \ "assignmentsCount").write[Long]
    )(unlift(GroupStats.unapply))

}