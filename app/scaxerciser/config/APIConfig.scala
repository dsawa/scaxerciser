package scaxerciser.config

object APIConfig extends ConfigReader {

  val ScaxerciserAnalyzeToken = getStringFromConf("scaxerciser.api.scaxerciser_analyze.token", "")

}