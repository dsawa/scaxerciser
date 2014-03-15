name := "scaxerciser"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.mongodb" %% "casbah" % "2.6.3",
  "com.novus" %% "salat" % "1.9.5",
  "commons-codec" % "commons-codec" % "1.6",
  "com.github.t3hnar" % "scala-bcrypt_2.10" % "2.3",
  "jp.t2v" %% "play2-auth"      % "0.11.0",
  "jp.t2v" %% "play2-auth-test" % "0.11.0" % "test",
  "org.scalatest" % "scalatest_2.10" % "2.1.0" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "latest.integration",
  "org.mockito" % "mockito-core" % "1.9.5" % "test"
)

play.Project.playScalaSettings