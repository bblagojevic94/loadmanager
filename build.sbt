import Dependencies._

lazy val buildSettings = Seq(
  organization := "io.mainflux",
  name := "loadmanager",
  version := "1.0.0",
  scalaVersion := "2.11.11"
)

lazy val dbLibs = Seq(postgres, slick, slickEvolutions)

lazy val utils = Seq(webJarsPlay, swagger, jodaTime)

lazy val testLibs = Seq(scalaTest, mockito)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(buildSettings: _*)
  .settings(
    libraryDependencies ++= (dbLibs ++ utils ++ testLibs :+ ws)
  )
