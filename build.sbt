import Dependencies._

lazy val buildSettings = Seq(
  organization := "io.mainflux",
  name := "loadmanager",
  version := "1.0.0",
  scalaVersion := "2.11.11"
)

lazy val coreLibs =
  Seq(ws, postgres, slick, slickEvolutions, akkaActor, akkaLogging, logback, webJarsPlay, swagger)
lazy val xmlLibs  = Seq(scalaXml, scalaParser, dispatch)
lazy val testLibs = Seq(scalaTest, mockito).map(_ % Test)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, ScalaxbPlugin)
  .settings(buildSettings: _*)
  .settings(
    libraryDependencies ++= (coreLibs ++ xmlLibs ++ testLibs)
  )
  .settings(
    scalaxbDispatchVersion in (Compile, scalaxb) := dispatch.revision,
    scalaxbPackageName in (Compile, scalaxb) := "generated",
    scalaxbXsdSource in (Compile, scalaxb) := baseDirectory.value / "conf" / "osgp" / "xsd",
    scalaxbWsdlSource in (Compile, scalaxb) := baseDirectory.value / "conf" / "osgp" / "wsdl"
  )
