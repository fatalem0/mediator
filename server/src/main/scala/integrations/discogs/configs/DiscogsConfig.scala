package integrations.discogs.configs

import cats.syntax.either._
import pureconfig.error.CannotConvert
import pureconfig.{ConfigReader, ConfigWriter}
import sttp.model.Uri

final case class DiscogsConfig(
    uri: Uri,
    consumerKey: String,
    consumerSecret: String
)

object DiscogsConfig {
  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader[String].emap(s =>
      Uri.parse(s).leftMap(CannotConvert(s, "sttp.model.Uri", _))
    )

  implicit val uriWriter: ConfigWriter[Uri] =
    ConfigWriter[String].contramap(_.toString)
}
