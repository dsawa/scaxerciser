package models

sealed trait Permission

case object Administrator extends Permission

case object Educator extends Permission

case object NormalUser extends Permission

object Permission {

  val GroupOwners = List(Administrator)
  val GroupEducators = List(Educator, Administrator)
  val GroupNormalUsers = List(NormalUser)

  def valueOf(value: String): Permission = value match {
    case "Administrator" => Administrator
    case "Educator" => Educator
    case "NormalUser" => NormalUser
    case _ => throw new IllegalArgumentException()
  }

}
