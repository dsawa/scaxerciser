package models.rabbitmq

import com.rabbitmq.client.MessageProperties
import scaxerciser.config.MQConfig

object SolutionSender {

  private val queueDurable = true


  def sendToAnalyze(solutionId: String) {
    val sendingChannel = RabbitMQConnection.connection.createChannel()

    sendingChannel.queueDeclare(MQConfig.RabbitMQSolutionsToAnalyzeQueue, queueDurable, false, false, null)
    sendingChannel.basicPublish("", MQConfig.RabbitMQSolutionsToAnalyzeQueue, MessageProperties.PERSISTENT_TEXT_PLAIN, solutionId.getBytes)
    sendingChannel.close()
  }

}
