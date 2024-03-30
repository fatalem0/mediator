package utils.errors

import derevo.derive
import derevo.tethys.{ tethysReader, tethysWriter }
import io.estatico.newtype.macros.newtype
import sttp.tapir.Schema

object Domain {
  @derive(tethysReader, tethysWriter)
  @newtype final case class ErrorMsg(value: String)

  object ErrorMsg {
    implicit val schema: Schema[ErrorMsg] = Schema.string[ErrorMsg]
  }

  @derive(tethysReader, tethysWriter)
  @newtype final case class ErrorCode(value: String)

  object ErrorCode {
    implicit val schema: Schema[ErrorCode] = Schema.string[ErrorCode]
  }
}
