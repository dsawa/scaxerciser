package actors

sealed trait SocketMessage

case class StartSocket(userId: String) extends SocketMessage

case class SocketClosed(userId: String) extends SocketMessage

case class Notify(solution: models.Solution) extends SocketMessage
