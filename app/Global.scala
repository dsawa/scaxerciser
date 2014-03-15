import java.io.File
import play.api._
import com.typesafe.config.ConfigFactory
import com.github.t3hnar.bcrypt._
import com.mongodb.casbah.Imports._
import models.{Administrator, Account}

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    if (app.mode.toString.toLowerCase != "test" && Account.all().isEmpty) {
      val adminEmail = "admin@example.com"
      val adminPassword = "admin".bcrypt(generateSalt)
      val adminPermission = Administrator.toString
      val adminAccount = new Account(new ObjectId, adminEmail, adminPassword, adminPermission)
      Account.create(adminAccount)
    }
  }

  override def onLoadConfig(config: Configuration, path: File, classloader: ClassLoader, mode: Mode.Mode): Configuration = {
    val modeSpecificConfig = config ++ Configuration(ConfigFactory.load(s"application.${mode.toString.toLowerCase}.conf"))
    super.onLoadConfig(modeSpecificConfig, path, classloader, mode)
  }

}