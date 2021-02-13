import Dependencies._

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.1.0"
ThisBuild / name := "temperature"
ThisBuild / organization := "com.martyphee"
ThisBuild / organizationName := "Temperature"

idePackagePrefix := Some("com.martyphee")

resolvers += Resolver.sonatypeRepo("snapshots")

lazy val flywaySettings = Seq(
  flywayUrl := "jdbc:postgresql://localhost:5432/temperature",
  flywayUser := "postgres",
  flywayPassword := "",
  flywayUrl in Test := "jdbc:postgresql://localhost:5432/temperature",
  flywayUser in Test := "postgres",
  flywayPassword in Test := "",
  flywayBaselineOnMigrate := true
)

lazy val root = (project in file("."))
  .settings(
    name := "temperature"
  )
  .aggregate(core, tests)

lazy val tests = (project in file("modules/tests"))
  .configs(IntegrationTest)
  .settings(
    name := "temperature-test-suite",
    scalacOptions += "-Ymacro-annotations",
    scalafmtOnCompile := true,
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      compilerPlugin(Libraries.kindProjector cross CrossVersion.full),
      compilerPlugin(Libraries.betterMonadicFor),
      Libraries.scalaCheck,
      Libraries.scalaTest,
      Libraries.scalaTestPlus
    )
  )
  .dependsOn(core)

lazy val core = (project in file("modules/core"))
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    name := "temperature-core",
    packageName in Docker := "temperature",
    scalacOptions += "-Ymacro-annotations",
    scalafmtOnCompile := true,
    resolvers += Resolver.sonatypeRepo("snapshots"),
    Defaults.itSettings,
    dockerBaseImage := "openjdk:8u201-jre-alpine3.9",
    dockerExposedPorts ++= Seq(8080),
    makeBatScripts := Seq(),
    dockerUpdateLatest := true,
    libraryDependencies ++= Seq(
      compilerPlugin(Libraries.kindProjector cross CrossVersion.full),
      compilerPlugin(Libraries.betterMonadicFor),
      Libraries.cats,
      Libraries.catsEffect,
      Libraries.catsMeowMtl,
      Libraries.catsRetry,
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.circeParser,
      Libraries.circeRefined,
      Libraries.cirisCore,
      Libraries.cirisEnum,
      Libraries.cirisRefined,
      Libraries.fs2,
      Libraries.http4sDsl,
      Libraries.http4sServer,
      Libraries.http4sCirce,
      Libraries.http4sJwtAuth,
      Libraries.log4cats,
      Libraries.logback % Runtime,
      Libraries.newtype,
      Libraries.refinedCore,
      Libraries.refinedCats,
      Libraries.skunkCore,
      Libraries.skunkTracing,
      Libraries.skunkCirce,
      Libraries.squants
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
