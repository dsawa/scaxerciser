package models.statistics

import com.mongodb.casbah.Imports._
import models.{Group, Assignment, Solution, Account, Educator, NormalUser}

object StatisticsCounter {

  def calculateForGroup(group: Group): GroupStats = {
    GroupStats(
      groupId = group.id.toString,
      name = group.name,
      membersCount = group.accountIds.size,
      normalUsersCount = group.groupRoles.filter(gr => gr.roleInGroup == NormalUser.toString).size,
      educatorsCount = group.groupRoles.filterNot(gr => gr.roleInGroup == NormalUser.toString).size,
      assignmentsCount = Assignment.count(MongoDBObject("groupId" -> group.id))
    )
  }

  def calculateAssignmentsStatsForGroup(group: Group): List[AssignmentStats] = {
    val assignments = group.assignments.all.map(assignment => Assignment.toObject(assignment))
    val assignmentIds = assignments.map(assignment => assignment.id)
    val assignmentMapping = assignments.foldLeft[Map[String, String]](Map())((m, a) => m ++ Map(a.id.toString -> a.title))

    val aggrMatch = MongoDBObject("$match" -> MongoDBObject("assignmentId" -> MongoDBObject("$in" -> assignmentIds)))
    val aggrGroup = MongoDBObject("$group" -> MongoDBObject("_id" -> "$assignmentId",
      "avgMark" -> MongoDBObject("$avg" -> "$result.mark"), "sumMark" -> MongoDBObject("$sum" -> "$result.mark")))
    val aggrProject = MongoDBObject("$project" -> MongoDBObject("_id" -> 0, "assignmentId" -> "$_id",
      "avgMark" -> 1, "sumMark" -> 1))

    Solution.solutionsCollection.aggregate(List(aggrMatch, aggrGroup, aggrProject)).results.
      map(res => {
      val assignmentId = res("assignmentId").asInstanceOf[ObjectId].toString

      AssignmentStats(
        assignmentId = assignmentId,
        name = assignmentMapping(assignmentId),
        avgMark = res("avgMark").asInstanceOf[Double],
        sumMark = res("sumMark").asInstanceOf[Double]
      )
    }).toList
  }

}
