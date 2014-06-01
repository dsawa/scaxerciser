package actors

import akka.actor.{ActorRef, Props, Actor}
import play.api.Play.current
import play.api.libs.iteratee.{Concurrent, Enumerator}
import play.api.libs.json.JsValue
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.concurrent.Akka
import play.api.Logger
import models.Solution

object SolutionNotifier {
  private val solutionNotifierActor = Akka.system.actorOf(props)

  def props: Props = Props(new SolutionNotifier)

  def getActor: ActorRef = solutionNotifierActor
}

class SolutionNotifier extends Actor {

  case class UserChannel(userId: String, var channelsCount: Int, enumerator: Enumerator[JsValue], channel: Channel[JsValue])

  lazy val log = Logger("application." + this.getClass.getSimpleName)

  var webSockets = Map[String, UserChannel]()

  override def receive = {
    case StartSocket(userId) =>
      log info s"start new socket for user $userId"

      // pobiera lub tworzy nową parę (Enumerator[JsValue], Channel[JsValue]) dla danego użytkownika
      // Channel pozwala na zapisanie danych do powiązanego enumeratora. To pozwala stworzyć WebSocket i przekazywać
      // mu dane właśnie poprzez pisanie do Channela
      val userChannel: UserChannel = webSockets.get(userId) getOrElse {
        val broadcast: (Enumerator[JsValue], Channel[JsValue]) = Concurrent.broadcast[JsValue]
        UserChannel(userId, 0, broadcast._1, broadcast._2)
      }

      // Jeśli dany użytkownika posiada, więcej niż jedno połączenie to zwiększa się licznik informujący o tym
      // zamiast tworzyć nową parę (Enumerator, Channel). Zwracany jest obecny enumerator i pisząc w tym jednym kanale
      // wszystkie otwarte websockety otrzymają te same dane
      userChannel.channelsCount = userChannel.channelsCount + 1
      webSockets += (userId -> userChannel)

      log info s"channel for user : $userId count : ${userChannel.channelsCount}"
      log info s"channel count : ${webSockets.size}"

      // zwróć enumerator powiązany z kanałem tego użytkowika
      sender ! userChannel.enumerator

    case SocketClosed(userId) =>
      log info s"closed socket for $userId"

      val userChannel = webSockets.get(userId).get

      if (userChannel.channelsCount > 1) {
        userChannel.channelsCount = userChannel.channelsCount - 1
        webSockets += (userId -> userChannel)
        log info s"channel for user : $userId count : ${userChannel.channelsCount}"
      } else {
        removeUserChannel(userId)
        log info s"removed channel for $userId"
      }

    case Notify(solution) =>
      webSockets.get(solution.userId.toString) match {
        case Some(userChannel) => userChannel.channel push Solution.toNormalUserJson(solution)
        case _ => None
      }
  }

  def removeUserChannel(userId: String) = webSockets -= userId

}
