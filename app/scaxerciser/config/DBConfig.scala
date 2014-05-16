package scaxerciser.config

object DBConfig extends ConfigReader {

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

}