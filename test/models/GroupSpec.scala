package models

import com.mongodb.casbah.Imports._
import play.api.Play
import play.api.test.FakeApplication
import org.scalatest.{FunSpec, Matchers, BeforeAndAfter, GivenWhenThen}

class GroupSpec extends FunSpec with BeforeAndAfter with Matchers with GivenWhenThen {

  lazy val collection = Group.groupsCollection

  val testGroupIds = List(new ObjectId, new ObjectId)

  before {
    Play.start(FakeApplication())
    val testGroup = Group(testGroupIds.head, "Test Group")
    val anotherTestGroup = Group(testGroupIds.last, "Another test Group")
    collection.insert(Group.toDBObject(testGroup))
    collection.insert(Group.toDBObject(anotherTestGroup))
  }

  after {
    collection.dropCollection()
    Play.stop()
  }

  describe("Group.create") {
    it("should save new group in database") {
      val beforeCount = collection.count()
      val newTestGroup = Group(new ObjectId, "New test group")

      When("Group is being inserted in database")
      Group.create(newTestGroup)

      Then("collection count increases")
      collection.count() shouldEqual (beforeCount + 1)

      And("We can find that group in database")
      val optionGroup = Group.findOneById(newTestGroup.id)

      optionGroup.isDefined shouldEqual true
      optionGroup.get.name shouldEqual "New test group"
    }

    it("should return Option with new good ObjectId inside") {
      val newTestGroup = Group(new ObjectId, "New test group")
      val result = Group.create(newTestGroup)

      result.isDefined shouldBe true
      newTestGroup.id shouldEqual result.get
    }
  }

  describe("Group.all") {
    it("should return List that contains Group elements") {
      When("there are some documents in database")
      val result = Group.all()
      val resultIds = result.map(group => group.id)

      Then("list size is equal to collection.count()")
      result.size should be > 0
      result.size shouldEqual collection.count()

      And("returned groups in list have expected ids")
      resultIds should contain only (testGroupIds.head, testGroupIds.last)
    }

    it("should return an empty List") {
      When("there isn't any document in database")
      collection.remove(MongoDBObject.empty)

      Group.all() shouldBe empty
    }
  }

  describe("Group.updateAttributes") {
    it("should update group attributes in database") {
      val groupToUpdate = Group.findOneById(testGroupIds.head).get
      val count = Group.updateAttributes(groupToUpdate.copy(name = "Updated group")).getN
      val updatedGroup = Group.findOneById(testGroupIds.head).get

      count shouldEqual 1
      updatedGroup.id shouldEqual groupToUpdate.id
      updatedGroup.accountIds shouldEqual groupToUpdate.accountIds
      updatedGroup.name should not equal groupToUpdate.name
      updatedGroup.name shouldEqual "Updated group"
    }

    it("should not upsert new document if Group with given id was not found") {
      val beforeCount = collection.count()
      val completelyNewGroup = Group(new ObjectId, "Completely new group")
      val wr = Group.updateAttributes(completelyNewGroup)

      wr.getN shouldEqual 0
      collection.count() shouldEqual beforeCount
    }
  }

}
