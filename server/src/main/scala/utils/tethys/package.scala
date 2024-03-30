package utils

import _root_.tethys.{ JsonReader, JsonWriter }

import java.time.Instant
import java.util.UUID

package object tethys {
  implicit val instantReader: JsonReader[Instant] =
    JsonReader.stringReader.map(Instant.parse)
  implicit val instantWriter: JsonWriter[Instant] =
    JsonWriter.stringWriter.contramap(_.toString)

  implicit val uuidReader: JsonReader[UUID] =
    JsonReader.stringReader.map(UUID.fromString)
  implicit val uuidWriter: JsonWriter[UUID] =
    JsonWriter.stringWriter.contramap(_.toString)
}
