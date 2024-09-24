import Dependencies.*

ThisBuild / scalaVersion := "3.5.0"

lazy val synonyms = (project in file("."))
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(
    name        := "synonyms",
    fork        := true,
    Test / fork := true,
    javaOptions += "-Xmx2G",
    dockerExposedPorts ++= Seq(8080),
    dockerUpdateLatest                     := true,
    dockerBaseImage                        := "eclipse-temurin:17-jre",
    makeBatScripts                         := Seq(),
    Compile / mainClass                    := Some("synonyms.SynonymsApi"),
    Compile / packageDoc / publishArtifact := false,
    testFrameworks += new TestFramework("munit.Framework"),
    libraryDependencies ++= Seq(
      Libraries.catsCore,
      Libraries.catsEffect,
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.circeParser,
      Libraries.declineEffect,
      Libraries.fs2core,
      Libraries.fs2dataJson,
      Libraries.fs2dataJsonCirce,
      Libraries.http4sBlazeClient,
      Libraries.http4sBlazeServer,
      Libraries.http4sCirce,
      Libraries.http4sDsl,
      Libraries.http4sEmberClient,
      Libraries.iron,
      Libraries.scalaScraper,
      Libraries.fs2io                 % Test,
      Libraries.munit                 % Test,
      Libraries.munitScalacheck       % Test,
      Libraries.munitCatsEffect       % Test,
      Libraries.scalacheckEffectMunit % Test
    )
  )
