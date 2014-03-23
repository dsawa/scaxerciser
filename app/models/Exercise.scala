package models

import play.api.libs.json.{JsPath, Reads}
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Exercise(description: String, hint: String = "")

object Exercise {
  implicit val exerciseReads: Reads[Exercise] = (
    (JsPath \ "description").read[String](minLength[String](1)) and
      (JsPath \ "hint").read[String]
    )(Exercise.apply _)
}
