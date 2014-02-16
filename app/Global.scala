import play.api._
import com.github.t3hnar.bcrypt._
import com.mongodb.casbah.Imports._
import models.{Administrator, Account}

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    if (Account.all().isEmpty) {
      val adminEmail = "admin@example.com"
      val adminPassword = "admin".bcrypt(generateSalt)
      val adminPermission = Administrator.toString
      val adminAccount = new Account(new ObjectId, adminEmail, adminPassword, adminPermission)
      Account.create(adminAccount)
    }
  }

}