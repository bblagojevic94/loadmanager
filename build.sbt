import Dependencies._

lazy val commonSettings = Seq(
  name := "loadmanager",
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
  scalaVersion := "2.12.2",
  version := "0.3.0"
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    coverageExcludedPackages := """controllers\..*Reverse.*;router.Routes.*;""",

    javaOptions in Universal += "-Dlogger.resource=logback-prod.xml",

    libraryDependencies ++= {
      val core = Seq(
        guice,
        logback,
        logstashEncoder,
        postgres,
        slick,
        slickEvolutions,
        swagger,
        webJarsPlay,
        ws
      )

      val testing = Seq(akkaTestKit, mockito, scalaTest, wiremock)

      core ++ testing.map(_ % Test)
    },

    dependencyOverrides ++= jetty.map(_ % Test),

    wartremoverExcluded := {
      val root = crossTarget.value
      Seq(
        root / "routes" / "main" / "router" / "Routes.scala",
        root / "routes" / "main" / "router" / "RoutesPrefix.scala",
        root / "routes" / "main" / "controllers" / "ReverseRoutes.scala",
        root / "routes" / "main" / "controllers" / "javascript" / "JavaScriptReverseRoutes.scala"
      )
    },

    wartremoverWarnings ++= Warts.unsafe.filterNot(_.clazz == Wart.NonUnitStatements.clazz)
)
