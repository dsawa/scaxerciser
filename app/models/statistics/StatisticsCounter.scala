package models.statistics

import com.mongodb.casbah.Imports._
import models.{Group, Assignment, Account, Educator, NormalUser}

object StatisticsCounter {

  def calculateForGroup(group: Group): GroupStats = {
    GroupStats(
      name = group.name,
      membersCount = group.accountIds.size,
      normalUsersCount = Account.count(MongoDBObject("groupIds" -> group.id, "permission" -> NormalUser.toString)),
      educatorsCount = Account.count(MongoDBObject("groupIds" -> group.id, "permission" -> Educator.toString)),
      assignmentsCount = Assignment.count(MongoDBObject("groupId" -> group.id))
    )

  }

}
