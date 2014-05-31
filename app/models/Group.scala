package models

import scaxerciser.context._
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import models.relations._
import models.statistics.{GroupStats, StatisticsCounter}
import scaxerciser.config.DBConfig

case class Group(@Key("_id") id: ObjectId, name: String, groupRoles: Set[GroupRole], accountIds: Set[ObjectId] = Set())
  extends OneToMany with ManyToMany {

  val db = DBConfig.groups("db")
  val collection = DBConfig.groups("collection")

  lazy val members = new ManyToManyRelation[Group, Account](this,
    Map("toDb" -> DBConfig.accounts("db"), "toCollection" -> DBConfig.accounts("collection"), "foreignIdsField" -> "accountIds"))

  lazy val assignments = new OneToManyRelation[Group, Assignment](this,
    Map("toDb" -> DBConfig.assignments("db"), "toCollection" -> DBConfig.assignments("collection")))

  def toDBObject = grater[Group].asDBObject(this)
}

object Group extends ModelCompanion[Group, ObjectId] {
  val groupsCollection = MongoConnection()(DBConfig.groups("db"))(DBConfig.groups("collection"))
  val dao = new SalatDAO[Group, ObjectId](collection = groupsCollection) {}

  def all(): List[Group] = Group.findAll().toList

  def create(newGroup: Group): Option[ObjectId] = Group.insert(newGroup)

  def updateAttributes(group: Group): WriteResult = {
    Group.update(
      q = MongoDBObject("_id" -> group.id),
      o = MongoDBObject("$set" -> MongoDBObject("name" -> group.name, "accountIds" -> group.accountIds)),
      upsert = false, multi = false, wc = Group.dao.collection.writeConcern
    )
  }

  def statistics(group: Group): GroupStats = StatisticsCounter.calculateForGroup(group)

  def hasUserPermission(group: Group, user: Account, authorityKeys: List[Permission]): Boolean = {
    group.groupRoles.find(gr => gr.accountId == user.id) match {
      case Some(groupRole) => authorityKeys.exists(perm => perm == Permission.valueOf(groupRole.roleInGroup))
      case None => false
    }
  }
}