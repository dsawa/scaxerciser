package scaxerciser.config

object MQConfig extends ConfigReader {

  val RabbitMQHost = getStringFromConf("rabbitmq.host", default = "localhost")
  val RabbitMQSolutionsToAnalyzeQueue = getStringFromConf("rabbitmq.solutions.queue", default = "message_queue")
  val RabbitMQSolutionsToAnalyzeExchange = getStringFromConf("rabbitmq.solutions.exchange", default = "message_exchange")

}
