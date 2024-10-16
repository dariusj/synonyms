import sbt.*

object Dependencies {

  object V {
    val cats                  = "2.12.0"
    val catsEffect            = "3.5.4"
    val catsLaws              = "2.12.0"
    val circe                 = "0.14.10"
    val decline               = "2.4.1"
    val disciplineMunit       = "2.0.0"
    val fs2                   = "3.11.0"
    val fs2data               = "1.11.1"
    val http4s                = "0.23.28"
    val http4sBlaze           = "0.23.16"
    val iron                  = "2.6.0"
    val log4cats              = "2.7.0"
    val logback               = "1.5.10"
    val monocle               = "3.3.0"
    val munit                 = "1.0.2"
    val munitCatsEffect       = "2.0.0"
    val munitScalacheck       = "1.0.0"
    val scalaScraper          = "3.1.1"
    val scalacheckEffectMunit = "1.0.4"
  }

  object Libraries {
    def circe(artifact: String)   = "io.circe"  %% s"circe-$artifact"    % V.circe
    def fs2data(artifact: String) = "org.gnieh" %% s"fs2-data-$artifact" % V.fs2data

    val catsCore   = "org.typelevel" %% "cats-core"   % V.cats
    val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect

    val circeCore    = circe("core")
    val circeGeneric = circe("generic")
    val circeParser  = circe("parser")

    val declineEffect = "com.monovore" %% "decline-effect" % V.decline

    val fs2dataJson      = fs2data("json")
    val fs2dataJsonCirce = fs2data("json-circe")

    val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % V.http4sBlaze
    val http4sCirce       = "org.http4s" %% "http4s-circe"        % V.http4s
    val http4sDsl         = "org.http4s" %% "http4s-dsl"          % V.http4s
    val http4sEmberClient = "org.http4s" %% "http4s-ember-client" % V.http4s
    val http4sEmberServer = "org.http4s" %% "http4s-ember-server" % V.http4s

    val iron = "io.github.iltotore" %% "iron" % V.iron

    val log4cats = "org.typelevel" %% "log4cats-slf4j"  % V.log4cats
    val logback  = "ch.qos.logback" % "logback-classic" % V.logback

    val monocleCore  = "dev.optics" %% "monocle-core"  % V.monocle
    val monocleMacro = "dev.optics" %% "monocle-macro" % V.monocle

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
