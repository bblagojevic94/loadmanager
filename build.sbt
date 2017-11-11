import Dependencies._

lazy val buildSettings = Seq(
  organization := "io.mainflux",
  name := "loadmanager",
  version := "1.0.0",
  scalaVersion := "2.11.11"
)

lazy val coreLibs = Seq(ws, scalaTest, mockito)

lazy val dbLibs = Seq(postgres, slick, slickEvolutions)

lazy val utils = Seq(webJarsPlay, swagger)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(buildSettings: _*)
  .settings(
    libraryDependencies ++= (coreLibs ++ dbLibs ++ utils)
  )
