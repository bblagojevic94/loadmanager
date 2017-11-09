lazy val buildSettings = Seq(
  organization := "io.mainflux",
  name := "loadmanager",
  version := "1.0.0",
  scalaVersion := "2.11.11"
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(buildSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      ws,
      "org.webjars"            %% "webjars-play"       % "2.6.1",
      "org.webjars"            % "swagger-ui"          % "3.2.2",
      "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
    )
  )
