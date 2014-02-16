package models

//import com.novus.salat.global._

import scaxerciser.context._
import com.github.t3hnar.bcrypt._
import com.mongodb.casbah.Imports._
import com.novus.salat.annotations._
import com.novus.salat.dao.{SalatDAO, ModelCompanion}

case class Account(@Key("_id") id: ObjectId, email: String, password: String, permission: String)

object Account extends ModelCompanion[Account, ObjectId] {
  val accountsCollection = MongoConnection()("scaxerciser")("users")
  val dao = new SalatDAO[Account, ObjectId](collection = accountsCollection) {}

  def all(): List[Account] = Account.findAll().toList

  def create(newAccount: Account): Option[ObjectId] = Account.insert(newAccount)

  def findByEmail(email: String): Option[Account] = {
    Account.findOne(MongoDBObject("email" -> email))
  }

  def authenticate(email: String, password: String): Option[Account] = {
    Account.findByEmail(email) match {
      case Some(account) => if (password.isBcrypted(account.password)) Some(account) else None
      case None => None
    }
  }

}
