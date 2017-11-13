import sbt._

object Dependencies {
  private object Versions {
    val postgres = "9.4.1207"
    val slick    = "2.1.0"
    val webJars  = "2.6.1"
    val swagger  = "3.2.2"

    val scalaTest = "2.0.0"
    val mockito   = "1.10.19"
  }

  val postgres: ModuleID        = "org.postgresql"    % "postgresql"             % Versions.postgres
  val slick: ModuleID           = "com.typesafe.play" %% "play-slick"            % Versions.slick
  val slickEvolutions: ModuleID = "com.typesafe.play" %% "play-slick-evolutions" % Versions.slick

  val webJarsPlay: ModuleID = "org.webjars" %% "webjars-play" % Versions.webJars
  val swagger: ModuleID     = "org.webjars" % "swagger-ui"    % Versions.swagger

  val scalaTest: ModuleID = "org.scalatestplus.play" %% "scalatestplus-play" % Versions.scalaTest
  val mockito: ModuleID   = "org.mockito"            % "mockito-all"         % Versions.mockito

}
