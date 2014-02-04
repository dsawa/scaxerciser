name := "scaxerciser"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.mongodb" %% "casbah" % "2.6.3",
  "com.novus" %% "salat" % "1.9.5",
  "commons-codec" % "commons-codec" % "1.6",
  cache
)     

play.Project.playScalaSettings

EclipseKeys.withSource := true