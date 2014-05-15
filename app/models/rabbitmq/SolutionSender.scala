package models.rabbitmq

import scaxerciser.config.MQConfig

object SolutionSender {

  def sendToAnalyze(solutionId: String) {
    val sendingChannel = RabbitMQConnection.connection.createChannel()

    sendingChannel.queueDeclare(MQConfig.RabbitMQSolutionsToAnalyzeQueue, false, false, false, null)
    sendingChannel.basicPublish("", MQConfig.RabbitMQSolutionsToAnalyzeQueue, null, solutionId.getBytes)
  }

}
