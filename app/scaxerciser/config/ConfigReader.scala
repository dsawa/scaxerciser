package scaxerciser.config

import play.api.Play
import play.api.Play.current

trait ConfigReader {

  def getStringFromConf(path: String, default: String = ""): String = {
    Play.configuration.getString(path) match {
      case Some(str) => str
      case None => default
    }
  }

  def getIntFromConf(path: String, default: Int = 0): Int = {
    Play.configuration.getInt(path) match {
      case Some(value) => value
      case None => default
    }
  }

}
