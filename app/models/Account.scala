package models

//import com.novus.salat.global._
import scaxerciser.context._
import com.github.t3hnar.bcrypt._
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import models.relations._

case class Account(@Key("_id") id: ObjectId, email: String, password: String, permission: String, groupIds: Set[ObjectId] = Set())
  extends RelationalDocument {

  val db = DBConfig.accounts("db")
  val collection = DBConfig.accounts("collection")
  val foreignIdsPropertyName = "groupIds"

  lazy val groups = new ManyToMany[Account, Group](this, Map("toDb" -> DBConfig.groups("db"), "toCollection" -> DBConfig.groups("collection")))

  def toDBObject = grater[Account].asDBObject(this)
}

object Account extends ModelCompanion[Account, ObjectId] {
  val accountsCollection = MongoConnection()(DBConfig.accounts("db"))(DBConfig.accounts("collection"))
  val dao = new SalatDAO[Account, ObjectId](collection = accountsCollection) {}

  def all(): List[Account] = Account.findAll().toList

  def create(newAccount: Account): Option[ObjectId] = Account.insert(newAccount)

  def updateAttributes(account: Account): WriteResult = {
    Permission.valueOf(account.permission)
    Account.update(
      q = MongoDBObject("_id" -> account.id),
      o = MongoDBObject("$set" -> MongoDBObject(
        "email" -> account.email, "password" -> account.password, "permission" -> account.permission, "groupIds" -> account.groupIds
      )),
      upsert = false, multi = false, wc = Account.dao.collection.writeConcern
    )
  }

  def destroy(account: Account): WriteResult = {
    val writeResult = account.groups.removeAll(account.groupIds)
    if (writeResult.getN > 0) Account.remove(account.copy(groupIds = Set())) else writeResult
  }

  def findByEmail(email: String): Option[Account] = {
    Account.findOne(MongoDBObject("email" -> email))
  }

  def authenticate(email: String, password: String): Option[Account] = {
    Account.findByEmail(email) match {
      case Some(account) => if (password.isBcrypted(account.password)) Some(account) else None
      case None => None
    }
  }

  def isAdmin(account: Account): Boolean = Permission.valueOf(account.permission) == Administrator

}
