import Dependencies.*

ThisBuild / scalaVersion := "3.5.1"

lazy val synonyms = (project in file(".")).aggregate(core, cli, http)

lazy val core = project
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      Libraries.catsCore,
      Libraries.catsEffect,
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.http4sEmberClient,
      Libraries.fs2dataJson,
      Libraries.fs2dataJsonCirce,
      Libraries.iron,
      Libraries.monocleCore,
      Libraries.monocleMacro,
      Libraries.scalaScraper,
      Libraries.catsLaws              % Test,
      Libraries.disciplineMunit       % Test,
      Libraries.http4sDsl             % Test,
      Libraries.munit                 % Test,
      Libraries.munitCatsEffect       % Test,
      Libraries.munitScalacheck       % Test,
      Libraries.scalacheckEffectMunit % Test
    )
  )

lazy val cli = project
  .dependsOn(core % "compile->compile;test->test")
  .settings(commonSettings)
  .settings(
    name := "synonyms-cli",
    libraryDependencies ++= Seq(Libraries.declineEffect, Libraries.logback % Runtime)
  )

lazy val http = project
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .dependsOn(core % "compile->compile;test->test")
  .settings(commonSettings)
  .settings(
    dockerExposedPorts ++= Seq(8080),
    dockerUpdateLatest                     := true,
    dockerUsername                         := Some("dariusj"),
    dockerBaseImage                        := "eclipse-temurin:17-jre",
    makeBatScripts                         := Seq(),
    packageName                            := "synonyms",
    Compile / packageDoc / publishArtifact := false,
    libraryDependencies ++= Seq(
      Libraries.http4sBlazeServer,
      Libraries.http4sCirce,
      Libraries.http4sDsl,
      Libraries.http4sEmberServer,
      Libraries.log4cats,
      Libraries.logback % Runtime
    )
  )

val commonSettings = Seq(
  semanticdbEnabled := true,
  fork              := true,
  javaOptions += "-Xmx2G",
  testFrameworks += new TestFramework("munit.Framework")
)
addCommandAlias(
  "testCi",
  List("scalafmtSbtCheck", "scalafmtCheckAll", "scalafixAll --check", "test").mkString("; ")
)
