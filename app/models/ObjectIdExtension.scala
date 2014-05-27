package models

import play.api.libs.json._
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import com.mongodb.casbah.Imports.ObjectId

object ObjectIdExtension {

  implicit val objectIdFormat: Format[ObjectId] = new Format[ObjectId] {
    def reads(json: JsValue) = {
      json match {
        case jsString: JsString => JsSuccess(new ObjectId(jsString.value))
        case whatever => JsError("Can't parse json path as an ObjectId. Json content = " + whatever.toString())
      }
    }

    def writes(oId: ObjectId): JsValue = {
      JsString(oId.toString)
    }
  }

}
