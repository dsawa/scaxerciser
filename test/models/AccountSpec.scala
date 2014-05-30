package models

import com.mongodb.casbah.Imports._
import com.github.t3hnar.bcrypt._
import play.api.Play
import play.api.test.FakeApplication
import org.scalatest.{FunSpec, Matchers, BeforeAndAfter, GivenWhenThen}

class AccountSpec extends FunSpec with BeforeAndAfter with GivenWhenThen with Matchers {

  lazy val collection = Account.accountsCollection

  val testAccountIds = List(new ObjectId, new ObjectId)
  val groupId = new ObjectId
  val adminId = new ObjectId
  val adminOwnerGroupRole = GroupRole(adminId, "Administrator")

  before {
    Play.start(FakeApplication())
    val group = Group(groupId, "Test Group", Set(adminOwnerGroupRole), Set(adminId))
    val testUser = Account(testAccountIds.head, "testAdmin@test.com", "qwerty".bcrypt(generateSalt), "Administrator", Set(groupId))
    val anotherTestUser = Account(testAccountIds.last, "testUser@test.com", "qwerty".bcrypt(generateSalt), "NormalUser")
    collection.insert(testUser.toDBObject)
    collection.insert(anotherTestUser.toDBObject)
    Group.groupsCollection.insert(group.toDBObject)
  }

  after {
    collection.dropCollection()
    Group.groupsCollection.dropCollection()
    Play.stop()
  }

  describe("Account.create") {
    it("should save new account data in database") {
      val beforeCount = collection.count()
      val newTestUser = Account(new ObjectId, "newUser@test.com", "qwerty", "NormalUser")

      When("User is being inserted in database")
      Account.create(newTestUser)

      Then("collection count increases")
      collection.count() shouldEqual (beforeCount + 1)

      And("We can find that user in database")
      val optionAccount = Account.findOneById(newTestUser.id)

      optionAccount.isDefined shouldEqual true
      optionAccount.get.email shouldEqual "newUser@test.com"
      optionAccount.get.permission shouldEqual "NormalUser"
    }

    it("should return Option with new good ObjectId inside") {
      val newTestUser = Account(new ObjectId, "newUser@test.com", "qwerty", "NormalUser")
      val result = Account.create(newTestUser)

      result.isDefined shouldBe true
      newTestUser.id shouldEqual result.get
    }
  }

  describe("Account.all") {
    it("should return List that contains Account elements") {
      When("there are some documents in database")
      val result = Account.all()
      val resultIds = result.map(account => account.id)

      Then("list size is equal to collection.count()")
      result.size should be > 0
      result.size shouldEqual collection.count()

      And("returned accounts in list have expected ids")
      resultIds should contain only(testAccountIds.head, testAccountIds.last)
    }

    it("should return an empty List") {
      When("there are not any of documents in database")
      collection.remove(MongoDBObject.empty)

      Account.all() shouldBe empty
    }
  }

  describe("Account.updateAttributes") {
    it("should update given account attributes in database") {
      val accountToUpdate = Account.findOneById(testAccountIds.head).get
      val count = Account.updateAttributes(accountToUpdate.copy(email = "updated@test.com", password = "asdfgh")).getN
      val updatedAccount = Account.findOneById(testAccountIds.head).get

      count shouldEqual 1
      updatedAccount.id shouldEqual accountToUpdate.id
      updatedAccount.groupIds shouldEqual accountToUpdate.groupIds
      updatedAccount.email should not equal accountToUpdate.email
      updatedAccount.password should not equal accountToUpdate.password
      updatedAccount.email shouldEqual "updated@test.com"
      updatedAccount.password shouldEqual "asdfgh"
    }

    it("should not upsert new document if Account with given id was not found") {
      val beforeCount = collection.count()
      val completelyNewUser = Account(new ObjectId, "newUser@test.com", "qwerty", "NormalUser")

      val wr = Account.updateAttributes(completelyNewUser)

      wr.getN shouldEqual 0
      collection.count() shouldEqual beforeCount
    }
  }

  describe("Account.isAdmin") {
    it("returns valid boolean") {
      Given("user that is admin")
      val admin = Account.findOneById(testAccountIds.head).get

      Then("answer is true")
      Account.isAdmin(admin) shouldEqual true

      Given("user that is not admin")
      val user = Account.findOneById(testAccountIds.last).get

      Then("answer is false")
      Account.isAdmin(user) shouldEqual false
    }

    it("should throw an IllegalArgumentException") {
      When("user permission field contains invalid value")
      val user = Account(new ObjectId, "email@abc.com", "qwerty", "WrongPerm")

      an[IllegalArgumentException] should be thrownBy Account.isAdmin(user)
    }
  }

  describe("Account.findByEmail") {
    it("should return Option with defined user data") {
      val result = Account.findByEmail("testAdmin@test.com")

      result.isDefined shouldEqual true
      result.get.email shouldEqual "testAdmin@test.com"
      result.get.permission shouldEqual "Administrator"
    }

    it("should return None if looking for user that not exists in database") {
      val result = Account.findByEmail("notExists@test.com")

      result.isDefined shouldEqual false
    }
  }

  describe("Account.authenticate") {
    it("should return Option with defined user data if authentications succeed") {
      val result = Account.authenticate("testAdmin@test.com", "qwerty")

      result.isDefined shouldEqual true
      result.get.email shouldEqual "testAdmin@test.com"
      result.get.permission shouldEqual "Administrator"
    }

    it("should return None authentication failed") {
      val result = Account.authenticate("testAdmin@test.com", "wrongPassword")

      result.isDefined shouldEqual false
    }
  }

  describe("Acount.destroy") {
    it("should remove user from database") {
      Given("user is in some groups")
      val admin = Account.findOneById(testAccountIds.head).get

      When("that user is removed from database")
      val result = Account.destroy(admin)

      Then("he is actually removed")
      result.getN shouldEqual 1
      Account.findOneById(admin.id) shouldEqual None

      val group = Group.findOneById(groupId).get

      And("group doesn't contain info about him anymore")
      group.accountIds should not contain admin.id
    }

    it("should also remove user without being in any of groups") {
      val user = Account.findOneById(testAccountIds.last).get
      val result = Account.destroy(user)

      result.getN shouldEqual 1
      Account.findOneById(user.id) shouldEqual None
    }
  }

}
