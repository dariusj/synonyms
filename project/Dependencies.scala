import sbt.*

object Dependencies {

  object V {
    val cats                  = "2.13.0"
    val catsEffect            = "3.6.3"
    val catsLaws              = "2.13.0"
    val circe                 = "0.14.15"
    val decline               = "2.5.0"
    val disciplineMunit       = "2.0.0"
    val fs2                   = "3.11.0"
    val fs2data               = "1.12.0"
    val http4s                = "0.23.30"
    val http4sBlaze           = "0.23.17"
    val iron                  = "3.2.0"
    val log4cats              = "2.7.1"
    val logback               = "1.5.19"
    val monocle               = "3.3.0"
    val munit                 = "1.1.1"
    val munitCatsEffect       = "2.1.0"
    val munitScalacheck       = "1.2.0"
    val scalaScraper          = "3.2.0"
    val scalacheckEffectMunit = "1.0.4"
  }

  object Libraries {
    def circe(artifact: String)   = "io.circe"   %% s"circe-$artifact"    % V.circe
    def fs2data(artifact: String) = "org.gnieh"  %% s"fs2-data-$artifact" % V.fs2data
    def http4s(artifact: String)  = "org.http4s" %% s"http4s-$artifact"   % V.http4s
    def monocle(artifact: String) = "dev.optics" %% s"monocle-$artifact"  % V.monocle

    val catsCore   = "org.typelevel" %% "cats-core"   % V.cats
    val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect

    val circeCore    = circe("core")
    val circeGeneric = circe("generic")
    val circeParser  = circe("parser")

    val declineEffect = "com.monovore" %% "decline-effect" % V.decline

    val fs2dataJson      = fs2data("json")
    val fs2dataJsonCirce = fs2data("json-circe")

    val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % V.http4sBlaze

    val http4sCirce       = http4s("circe")
    val http4sDsl         = http4s("dsl")
    val http4sEmberClient = http4s("ember-client")
    val http4sEmberServer = http4s("ember-server")

    val iron = "io.github.iltotore" %% "iron" % V.iron

    val log4cats = "org.typelevel" %% "log4cats-slf4j"  % V.log4cats
    val logback  = "ch.qos.logback" % "logback-classic" % V.logback

    val monocleCore  = monocle("core")
    val monocleMacro = monocle("macro")

    val scalaScraper = "net.ruippeixotog" %% "scala-scraper" % V.scalaScraper

    // Test
    val catsLaws        = "org.typelevel" %% "cats-laws"         % V.catsLaws
    val disciplineMunit = "org.typelevel" %% "discipline-munit"  % V.disciplineMunit
    val fs2io           = "co.fs2"        %% "fs2-io"            % V.fs2
    val munit           = "org.scalameta" %% "munit"             % V.munit
    val munitScalacheck = "org.scalameta" %% "munit-scalacheck"  % V.munitScalacheck
    val munitCatsEffect = "org.typelevel" %% "munit-cats-effect" % V.munitCatsEffect
    val scalacheckEffectMunit =
      "org.typelevel" %% "scalacheck-effect-munit" % V.scalacheckEffectMunit
  }
}
