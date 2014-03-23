package models

import com.mongodb.casbah.Imports._
import play.api.Play
import play.api.test.FakeApplication
import org.scalatest.{FunSpec, Matchers, BeforeAndAfter, GivenWhenThen}

class AssignmentSpec extends FunSpec with BeforeAndAfter with Matchers with GivenWhenThen {

  lazy val assignmentsCollection = Assignment.assignmentsCollection
  lazy val groupsCollection = Group.groupsCollection

  val testGroupId = new ObjectId
  val assignmentId = new ObjectId
  val assignmentTitle = "Recursion"
  val assignmentExercises = List(Exercise("Do function with tail recursion"))

  before {
    Play.start(FakeApplication())
    val assignment = Assignment(assignmentId, assignmentTitle, assignmentExercises, testGroupId)
    val testGroup = Group(testGroupId, "Test Group")
    assignmentsCollection.insert(Assignment.toDBObject(assignment))
    groupsCollection.insert(Group.toDBObject(testGroup))
  }

  after {
    assignmentsCollection.dropCollection()
    groupsCollection.dropCollection()
    Play.stop()
  }

  describe("Assignment.create") {
    it("should save new assignment in database") {
      val beforeCount = assignmentsCollection.count()
      val assignment = Assignment(new ObjectId, assignmentTitle + "2", assignmentExercises, testGroupId)

      When("Assignment is being inserted in database")
      Assignment.create(assignment)

      Then("collection count increases")
      assignmentsCollection.count() shouldEqual (beforeCount + 1)

      And("We can find that assignment in database")
      val optionGroup = Assignment.findOneById(assignment.id)

      optionGroup.isDefined shouldEqual true
      optionGroup.get.title shouldEqual assignmentTitle + "2"
    }

    it("should return Option with new good ObjectId inside") {
      val assignment = Assignment(new ObjectId, assignmentTitle + "2", assignmentExercises, testGroupId)
      val result =  Assignment.create(assignment)

      result.isDefined shouldBe true
      assignment.id shouldEqual result.get
    }
  }

  describe("Assignment.all") {
    it("should return List that contains assignments") {
      When("there are some documents in database")
      val result = Assignment.all()
      val resultIds = result.map(assignment => assignment.id)

      Then("list size is equal to collection.count()")
      result.size should be > 0
      result.size shouldEqual assignmentsCollection.count()

      And("returned groups in list have expected ids")
      resultIds should contain only assignmentId
    }

    it("should return an empty List") {
      When("there isn't any document in database")
      assignmentsCollection.remove(MongoDBObject.empty)

      Assignment.all() shouldBe empty
    }
  }

}
