scalaVersion := "3.4.2"

scalacOptions ++= Seq("-Wunused:all", "-Ykind-projector:underscores", "-deprecation")

testFrameworks += new TestFramework("munit.Framework")

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "3.10.2" ,
  "com.monovore" %% "decline-effect" % "2.4.1" ,
  "io.circe" %% "circe-core" % "0.14.9" ,
  "io.circe" %% "circe-generic" % "0.14.9" ,
  "io.circe" %% "circe-parser" % "0.14.9" ,
  "net.ruippeixotog" %% "scala-scraper" % "3.1.1" ,
  "org.gnieh" %% "fs2-data-json-circe" % "1.11.0" ,
  "org.gnieh" %% "fs2-data-json" % "1.11.0" ,
  "org.http4s" %% "http4s-blaze-client" % "0.23.16" ,
  "org.http4s" %% "http4s-blaze-server" % "0.23.16" ,
  "org.http4s" %% "http4s-circe" % "0.23.27" ,
  "org.http4s" %% "http4s-dsl" % "0.23.27" ,
  "org.http4s" %% "http4s-ember-client" % "0.23.27" ,
  "org.typelevel" %% "cats-core" % "2.12.0" ,
  "org.typelevel" %% "cats-effect" % "3.5.4" 
)

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-io" % "3.10.2" % Test,
  "org.scalameta" %% "munit-scalacheck" % "1.0.0" % Test,
  "org.scalameta" %% "munit" % "1.0.0" % Test,
  "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test,
  "co.fs2" %% "fs2-core" % "3.10.2" % Test,
  "com.monovore" %% "decline-effect" % "2.4.1" % Test,
  "io.circe" %% "circe-core" % "0.14.9" % Test,
  "io.circe" %% "circe-generic" % "0.14.9" % Test,
  "io.circe" %% "circe-parser" % "0.14.9" % Test,
  "net.ruippeixotog" %% "scala-scraper" % "3.1.1" % Test,
  "org.gnieh" %% "fs2-data-json-circe" % "1.11.0" % Test,
  "org.gnieh" %% "fs2-data-json" % "1.11.0" % Test,
  "org.http4s" %% "http4s-blaze-client" % "0.23.16" % Test,
  "org.http4s" %% "http4s-blaze-server" % "0.23.16" % Test,
  "org.http4s" %% "http4s-circe" % "0.23.27" % Test,
  "org.http4s" %% "http4s-dsl" % "0.23.27" % Test,
  "org.http4s" %% "http4s-ember-client" % "0.23.27" % Test,
  "org.typelevel" %% "cats-core" % "2.12.0" % Test,
  "org.typelevel" %% "cats-effect" % "3.5.4" % Test,
  "org.typelevel" %% "scalacheck-effect-munit" % "1.0.4" % Test
)

