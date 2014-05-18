package models

import com.rabbitmq.client.{Connection, ConnectionFactory}
import scaxerciser.config.MQConfig

object RabbitMQConnection {

  lazy val connection: Connection = {
    val factory = new ConnectionFactory()
    factory.setHost(MQConfig.RabbitMQHost)
    factory.newConnection()
  }

}
