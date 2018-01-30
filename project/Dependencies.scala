import sbt._

object Dependencies {
  private object Versions {
    val akka            = "2.5.4"
    val logback         = "1.2.3"
    val logstashEncoder = "4.11"
    val jetty           = "9.2.22.v20170606"
    val mockito         = "2.+"
    val postgres        = "9.4.1207"
    val scalaTest       = "3.0.4"
    val slick           = "3.0.0"
    val swagger         = "3.2.2"
    val webJars         = "2.6.1"
    val wiremock        = "2.14.0"
  }

  // dependency overrides required by Wiremock
  val jetty = Set(
    "org.eclipse.jetty" % "jetty-http" % Versions.jetty,
    "org.eclipse.jetty" % "jetty-io"   % Versions.jetty,
    "org.eclipse.jetty" % "jetty-util" % Versions.jetty
  )

  val akkaTestKit     = "com.typesafe.akka"      %% "akka-testkit"            % Versions.akka
  val logback         = "ch.qos.logback"         % "logback-classic"          % Versions.logback
  val logstashEncoder = "net.logstash.logback"   % "logstash-logback-encoder" % Versions.logstashEncoder
  val mockito         = "org.mockito"            % "mockito-all"              % Versions.mockito
  val postgres        = "org.postgresql"         % "postgresql"               % Versions.postgres
  val scalaTest       = "org.scalatest"          %% "scalatest"               % Versions.scalaTest
  val slick           = "com.typesafe.play"      %% "play-slick"              % Versions.slick
  val slickEvolutions = "com.typesafe.play"      %% "play-slick-evolutions"   % Versions.slick
  val swagger         = "org.webjars"            % "swagger-ui"               % Versions.swagger
  val webJarsPlay     = "org.webjars"            %% "webjars-play"            % Versions.webJars
  val wiremock        = "com.github.tomakehurst" % "wiremock"                 % Versions.wiremock
}
