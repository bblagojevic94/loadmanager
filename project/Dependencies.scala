import sbt._

object Dependencies {
  private object Versions {
    val slick = "2.1.0"
  }

  val postgres: ModuleID        = "org.postgresql"    % "postgresql"             % "9.4.1207"
  val slick: ModuleID           = "com.typesafe.play" %% "play-slick"            % Versions.slick
  val slickEvolutions: ModuleID = "com.typesafe.play" %% "play-slick-evolutions" % Versions.slick

  val webJarsPlay: ModuleID = "org.webjars" %% "webjars-play" % "2.6.1"
  val swagger: ModuleID     = "org.webjars" % "swagger-ui"    % "3.2.2"

  val scalaTest: ModuleID = "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0"   % Test
  val mockito: ModuleID   = "org.mockito"            % "mockito-all"         % "1.10.19" % Test

}
