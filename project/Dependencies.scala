import sbt._

object Dependencies {
  private object Versions {
    val postgres = "9.4.1207"
    val slick    = "2.1.0"

    val akka = "2.5.6"

    val webJars = "2.6.1"
    val swagger = "3.2.2"
    val logback = "1.2.3"

    val scalaXml = "1.0.6"
    val dispatch = "0.12.0"

    val scalaTest = "2.0.0"
    val mockito   = "1.10.19"
  }

  val postgres: ModuleID        = "org.postgresql"    % "postgresql"             % Versions.postgres
  val slick: ModuleID           = "com.typesafe.play" %% "play-slick"            % Versions.slick
  val slickEvolutions: ModuleID = "com.typesafe.play" %% "play-slick-evolutions" % Versions.slick

  val akkaActor: ModuleID   = "com.typesafe.akka" %% "akka-actor" % Versions.akka
  val akkaLogging: ModuleID = "com.typesafe.akka" %% "akka-slf4j" % Versions.akka

  val logback: ModuleID     = "ch.qos.logback" % "logback-classic" % Versions.logback
  val webJarsPlay: ModuleID = "org.webjars"    %% "webjars-play"   % Versions.webJars
  val swagger: ModuleID     = "org.webjars"    % "swagger-ui"      % Versions.swagger

  val scalaXml    = "org.scala-lang.modules"  %% "scala-xml"                % Versions.scalaXml
  val scalaParser = "org.scala-lang.modules"  %% "scala-parser-combinators" % Versions.scalaXml
  val dispatch    = "net.databinder.dispatch" %% "dispatch-core"            % Versions.dispatch

  val scalaTest: ModuleID   = "org.scalatestplus.play" %% "scalatestplus-play" % Versions.scalaTest
  val mockito: ModuleID     = "org.mockito"            % "mockito-all"         % Versions.mockito
  val akkaTestKit: ModuleID = "com.typesafe.akka"      %% "akka-testkit"       % Versions.akka

}
