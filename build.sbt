enablePlugins(JavaAppPackaging)

val Versions = new {
  val circe = "0.14.1"
  val http4s = "0.23.7"
  val jwtCirce = "9.4.3"
  val pureconfig = "0.17.1"
  val logback = "1.2.10"
  val scalaLogging = "3.9.4"
  val sttp = "2.3.0"
}

lazy val scalafmtSettings = Seq(
  scalafmtOnCompile := true
)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8"
)

lazy val root = project
  .in(file("."))
  .aggregate(auth_service, task_service)
  .settings(
    name := "asynchronous-architecture-track"
  )

lazy val auth_service = project
  .in(file("auth_service"))
  .settings(
    name := "auth-service",
    scalaVersion := "2.13.11",
    scalacOptions ++= compilerOptions,
    scalafmtSettings,
    libraryDependencies ++= Seq(
      "io.circe"                   %% "circe-generic"       % Versions.circe,
      "io.circe"                   %% "circe-generic-extras"       % Versions.circe,
      "org.http4s"                 %% "http4s-blaze-server" % Versions.http4s,
      "org.http4s"                 %% "http4s-dsl"          % Versions.http4s,
      "org.http4s"                 %% "http4s-circe"        % Versions.http4s,
      "com.github.jwt-scala" %% "jwt-circe" % Versions.jwtCirce,
      "com.github.pureconfig"      %% "pureconfig"          % Versions.pureconfig,
      "ch.qos.logback"              % "logback-classic"     % Versions.logback,
      "com.typesafe.scala-logging" %% "scala-logging"       % Versions.scalaLogging
    )
  )

lazy val task_service = project
  .in(file("task_service"))
  .settings(
    name := "task-service",
    scalaVersion := "2.13.11",
    scalacOptions ++= compilerOptions,
    scalafmtSettings,
    libraryDependencies ++= Seq(
      "io.circe"                   %% "circe-generic"       % Versions.circe,
      "io.circe"                   %% "circe-generic-extras"       % Versions.circe,

      "org.http4s"                 %% "http4s-blaze-server" % Versions.http4s,
      "org.http4s"                 %% "http4s-dsl"          % Versions.http4s,
      "org.http4s"                 %% "http4s-circe"        % Versions.http4s,
      "com.github.pureconfig"      %% "pureconfig"          % Versions.pureconfig,
      "ch.qos.logback"              % "logback-classic"     % Versions.logback,
      "com.typesafe.scala-logging" %% "scala-logging"       % Versions.scalaLogging
    )
  )
