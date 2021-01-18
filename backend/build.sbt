import Dependencies._
import Versions.zioLogging

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.martyphee"
ThisBuild / organizationName := "temp-monitor"

lazy val flywaySettings = Seq(
  flywayUrl := "jdbc:postgresql://localhost:5432/temp_monitor",
  flywayUser := "postgres",
  flywayPassword := "",
  flywayUrl in Test := "jdbc:postgresql://localhost:5432/temp_monitor_test",
  flywayUser in Test := "postgres",
  flywayPassword in Test := "",
  flywayBaselineOnMigrate := true
)

val commonJvmSettings: Seq[Def.Setting[_]] = commonSmlBuildSettings

lazy val dbDependencies = List(
  "org.tpolecat" %% "skunk-core" % "0.0.20"
)

lazy val root = (project in file("server"))
  .settings(commonJvmSettings)
  .settings(
    name := "temp-monitor-server",
    libraryDependencies ++= dbDependencies ++ Seq(
      scalaTest % Test,
      "dev.zio" %% "zio-interop-cats" % Versions.zioInteropCats,
      "org.typelevel" %% "cats-effect" % Versions.catsEffect,
      "com.typesafe" % "config" % "1.4.1",
      "org.http4s" %% "http4s-core" % Versions.http4s,
      "org.http4s" %% "http4s-dsl" % Versions.http4s,
      "org.http4s" %% "http4s-blaze-server" % Versions.http4s,
      "org.http4s" %% "http4s-circe" % Versions.http4s,
      "io.circe" %% "circe-generic" % Versions.circe,
      "is.cir" %% "ciris-cats" % Versions.ciris,
      "is.cir" %% "ciris-cats-effect" % Versions.ciris,
      "is.cir" %% "ciris-core" % Versions.ciris,
      "is.cir" %% "ciris-enumeratum" % Versions.ciris,
      "is.cir" %% "ciris-generic" % Versions.ciris,
      "is.cir" %% "ciris-refined" % Versions.ciris,
      "com.softwaremill.tapir" %% "tapir-core" % Versions.tapir,
      "com.softwaremill.tapir" %% "tapir-http4s-server" % Versions.tapir,
      "com.softwaremill.tapir" %% "tapir-swagger-ui-http4s" % Versions.tapir,
      "com.softwaremill.tapir" %% "tapir-openapi-docs" % Versions.tapir,
      "com.softwaremill.tapir" %% "tapir-openapi-circe-yaml" % Versions.tapir,
      "com.softwaremill.tapir" %% "tapir-json-circe" % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-zio" % "0.17.1"
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server" % "0.17.1"
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
    )
  )

lazy val migrations = project.in(file("migrations"))
  .enablePlugins(FlywayPlugin)
  .settings(flywaySettings: _*)
  .settings {
    flywayLocations += "db/migrations"
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "42.2.5"
    )
  }
