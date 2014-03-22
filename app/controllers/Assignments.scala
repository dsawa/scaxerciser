package controllers

import play.api.mvc.Controller
import com.mongodb.casbah.Imports.ObjectId
import jp.t2v.lab.play2.auth.AuthElement
import models._

object Assignments extends Controller with AuthElement with AuthConfigImpl {

  def index = StackAction(AuthorityKey -> NormalUser) {
    implicit request =>
      Ok("")
  }

  def create(groupId: String) = StackAction(AuthorityKey -> Administrator) {
    implicit request =>
      Group.findOneById(new ObjectId(groupId)) match {
        case Some(group) =>
          group.assignments
          Ok("")
        case None => Ok("")
      }
  }

}