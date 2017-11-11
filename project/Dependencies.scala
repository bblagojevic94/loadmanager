import sbt._

object Dependencies {
  private val slickVersion = "2.1.0"

  val postgres: ModuleID        = "org.postgresql"    % "postgresql"             % "9.4.1207"
  val slick: ModuleID           = "com.typesafe.play" %% "play-slick"            % slickVersion
  val slickEvolutions: ModuleID = "com.typesafe.play" %% "play-slick-evolutions" % slickVersion

  val jodaTime: ModuleID = "joda-time" % "joda-time" % "2.9.7"

  val webJarsPlay: ModuleID = "org.webjars" %% "webjars-play" % "2.6.1"
  val swagger: ModuleID     = "org.webjars" % "swagger-ui"    % "3.2.2"

  val scalaTest: ModuleID = "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0"   % Test
  val mockito: ModuleID   = "org.mockito"            % "mockito-all"         % "1.10.19" % Test

}
