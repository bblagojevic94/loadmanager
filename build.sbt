import Dependencies._

lazy val commonSettings = Seq(
  organization := "io.mainflux",
  organizationName := "Mainflux",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture",
    "-Ywarn-unused-import"
  ),
  scalaVersion := "2.11.11",
  version := "1.0.0-SNAPSHOT"
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    commonSettings,
    name := "loadmanager",

    libraryDependencies ++= {
      val core = Seq(
        guice,
        logback,
        postgres,
        slick,
        slickEvolutions,
        swagger,
        webJarsPlay
      )

      val testing = Seq(akkaTestKit, mockito, scalaTest).map(_ % Test)

      core ++ testing
    },

    wartremoverExcluded := {
      val root = crossTarget.value
      Seq(
        root / "routes" / "main" / "router" / "Routes.scala",
        root / "routes" / "main" / "router" / "RoutesPrefix.scala",
        root / "routes" / "main" / "controllers" / "ReverseRoutes.scala",
        root / "routes" / "main" / "controllers" / "javascript" / "JavaScriptReverseRoutes.scala"
      )
    },

    wartremoverWarnings ++= Warts.unsafe
  )
