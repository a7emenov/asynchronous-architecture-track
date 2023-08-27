enablePlugins(JavaAppPackaging)

val Versions = new {
  val http4s = "0.23.7"
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
  .aggregate(auth_service)
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
      "org.http4s"                 %% "http4s-blaze-server" % Versions.http4s,
      "org.http4s"                 %% "http4s-dsl"          % Versions.http4s,
      "org.http4s"                 %% "http4s-circe"        % Versions.http4s,
      "com.github.pureconfig"      %% "pureconfig-core"     % Versions.pureconfig,
      "ch.qos.logback"              % "logback-classic"     % Versions.logback,
      "com.typesafe.scala-logging" %% "scala-logging"       % Versions.scalaLogging
    )
  )
