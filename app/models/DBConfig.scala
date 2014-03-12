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