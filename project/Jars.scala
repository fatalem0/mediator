import sbt.librarymanagement.syntax._

object Jars {
  object cats {
    private val effectVersion = "3.5.4"

    val effect = "org.typelevel" %% "cats-effect" % effectVersion
  }

  object chimney {
    private val chimneyVersion = "0.8.5"

    val core = "io.scalaland" %% "chimney" % chimneyVersion
  }

  object derevo {
    private val derevoVersion = "0.13.0"

    val cats        = "tf.tofu" %% "derevo-cats"         % derevoVersion
    val catsTagless = "tf.tofu" %% "derevo-cats-tagless" % derevoVersion
    val pureconfig  = "tf.tofu" %% "derevo-pureconfig"   % derevoVersion
    val tethys      = "tf.tofu" %% "derevo-tethys"       % derevoVersion
  }

  object doobie {
    private val doobieVersion = "1.0.0-RC2"

    val core     = "org.tpolecat" %% "doobie-core"     % doobieVersion
    val hikari   = "org.tpolecat" %% "doobie-hikari"   % doobieVersion
    val postgres = "org.tpolecat" %% "doobie-postgres" % doobieVersion
  }

  object enumeratum {
    private val enumeratumVersion = "1.7.3"

    val core   = "com.beachape" %% "enumeratum"        % enumeratumVersion
    val doobie = "com.beachape" %% "enumeratum-doobie" % enumeratumVersion
  }

  object estatico {
    private val estaticoVersion = "0.4.4"

    val newtype = "io.estatico" %% "newtype" % estaticoVersion
  }

  object fs2 {
    private val fs2Version = "3.10.2"

    val core = "co.fs2" %% "fs2-core" % fs2Version
  }

  object http4s {
    private val http4sVersion = "0.23.26"
    private val blazeVersion = "0.23.16"

    val blazeServer = "org.http4s" %% "http4s-blaze-server" % blazeVersion
    val dsl =         "org.http4s" %% "http4s-dsl"          % http4sVersion
    val emberServer = "org.http4s" %% "http4s-ember-server" % http4sVersion
  }

  object profunktor {
    private val profunktorVersion = "1.2.2"

    val jwtAuth = "dev.profunktor" %% "http4s-jwt-auth" % profunktorVersion
  }

  object pureconfig {
    private val pureconfigVersion = "0.17.6"

    val core = "com.github.pureconfig" %% "pureconfig" % pureconfigVersion
  }

  object scalapass {
    private val scalapassVersion = "1.2.8"

    val core = "com.outr" %% "scalapass" % scalapassVersion
  }

  object sttp {
    private val sttpVersion = "3.9.5"

    val async = "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats"  % sttpVersion
    val core  = "com.softwaremill.sttp.client3" %% "core"                            % sttpVersion
  }

  object tapir {
    private val tapirVersion = "1.10.4"

    val core       = "com.softwaremill.sttp.tapir" %% "tapir-core"          % tapirVersion
    val enumeratum = "com.softwaremill.sttp.tapir" %% "tapir-enumeratum"    % tapirVersion
    val http4s     = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion
    val tethys     = "com.softwaremill.sttp.tapir" %% "tapir-json-tethys"   % tapirVersion
  }

  object tethys {
    private val tethysVersion = "0.26.0"

    val core       = "com.tethys-json" %% "tethys"            % tethysVersion
    val enumeratum = "com.tethys-json" %% "tethys-enumeratum" % tethysVersion
  }

  object tofu {
    private val tofuVersion = "0.12.0.1"

    val cats =    "tf.tofu" %% "tofu-core-ce3"           % tofuVersion
    val layout =  "tf.tofu" %% "tofu-logging-derivation" % tofuVersion
    val logging = "tf.tofu" %% "tofu-logging"            % tofuVersion
  }

  object testing {
    private val scalaTestVersion = "3.2.18"

    val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion
  }
}
