package endpoints

import cats.Applicative
import mediator.chat.ChatCreateService
import mediator.chat.Domain.ChatCreate
import mediator.chat.Domain.Errors.ChatCreateError
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.{ endpoint, Endpoint }
import utils.errors.ApiError
import utils.server.{ ApiBuilder, HttpModule, WireWithLogic }

trait ChatCreateController[F[_]] {
  def create(req: ChatCreate.Request): F[Either[ChatCreateError, Unit]]
}

object ChatCreateController {
  final private class Impl[F[_]: Applicative](service: ChatCreateService[F])
    extends ChatCreateController[F] {
    override def create(req: ChatCreate.Request): F[Either[
      ChatCreateError,
      Unit
    ]] = service.create(req)
  }

  object protocol {
    val create: Endpoint[
      Unit,
      ChatCreate.Request,
      ApiError,
      Unit,
      Any
    ] = endpoint
      .summary("Создание чата")
      .post
      .in(ApiV1 / "chat" / "create")
      .in(jsonBody[ChatCreate.Request])
      .errorOut(
        ApiError.makeOneOf[ChatCreateError](ChatCreateError.variants)
      )
  }

  implicit val wireWithLogic: WireWithLogic[ChatCreateController] =
    new WireWithLogic[ChatCreateController] {
      override def wire[F[_]](controller: ChatCreateController[F])(implicit
          builder: ApiBuilder[F]
      ): HttpModule[F] = List(
        builder.build(
          controller.create,
          protocol.create
        )
      )
    }

  def make[F[_]: Applicative](
      service: ChatCreateService[F]
  ): ChatCreateController[F] = new Impl[F](service)
}
