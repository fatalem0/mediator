package users.services.auth

import derevo.derive
import derevo.tethys.tethysReader
import tofu.{ Errors => TofuErrors }
import users.Domain.Password.UnhashedUserPassword
import users.Domain.UserEmail
import utils.tethys._

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class AuthError(
        val code: String
    ) extends Throwable
        with NoStackTrace

    object AuthError extends TofuErrors.Companion[AuthError] {
      final case class NotFound(
          email: UserEmail
      ) extends AuthError("NOT_FOUND")
    }
  }

  object Authentication {
    @derive(tethysReader)
    final case class Request(
        email: UserEmail,
        password: UnhashedUserPassword
    )
  }
}
