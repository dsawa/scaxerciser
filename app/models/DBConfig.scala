package models

import play.api.Play
import play.api.Play.current

object DBConfig {

  val defaultHost = getStringFromConf("mongo.connection.host", "localhost")
  val defaultPort = getIntFromConf("mongo.connection.port", 27017)

  val accounts = {
    Map("db" -> getStringFromConf("mongo.accounts.db"), "collection" -> getStringFromConf("mongo.accounts.collection"))
  }

  val groups = {
    Map("db" -> getStringFromConf("mongo.groups.db"), "collection" -> getStringFromConf("mongo.groups.collection"))
  }

  val assignments = {
    Map("db" -> getStringFromConf("mongo.assignments.db"), "collection" -> getStringFromConf("mongo.assignments.collection"))
  }

  val assignmentsProjects = {
    Map("db" -> getStringFromConf("mongo.assignments.projects.db"))
  }

  val solutions = {
    Map("db" -> getStringFromConf("mongo.solutions.db"), "collection" -> getStringFromConf("mongo.solutions.collection"))
  }

  val solutionsProjects = {
    Map("db" -> getStringFromConf("mongo.solutions.projects.db"))
  }

  private def getStringFromConf(path: String, default: String = ""): String = {
    Play.configuration.getString(path) match {
      case Some(str) => str
      case None => default
    }
  }

  private def getIntFromConf(path: String, default: Int = 0): Int = {
    Play.configuration.getInt(path) match {
      case Some(value) => value
      case None => default
    }
  }

}