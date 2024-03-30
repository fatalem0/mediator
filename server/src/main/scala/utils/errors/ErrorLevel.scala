package utils.errors

import cats.Order
import sttp.model.StatusCode

sealed abstract class ErrorLevel(val code: StatusCode)

object ErrorLevel {
  case object Unauthorized  extends ErrorLevel(StatusCode.Unauthorized)
  case object Forbidden     extends ErrorLevel(StatusCode.Forbidden)
  case object NotFound      extends ErrorLevel(StatusCode.NotFound)
  case object BadRequest    extends ErrorLevel(StatusCode.BadRequest)
  case object Business      extends ErrorLevel(StatusCode.UnprocessableEntity)
  case object Internal      extends ErrorLevel(StatusCode.InternalServerError)
  case object AlreadyExists extends ErrorLevel(StatusCode.Conflict)

  implicit val order: Order[ErrorLevel] =
    Order.by[ErrorLevel, Int](_.code.code)
}
