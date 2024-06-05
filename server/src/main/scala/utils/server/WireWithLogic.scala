package utils.server

trait WireWithLogic[Controller[_[_]]] {
  def wire[F[_]](controller: Controller[F])(implicit
      builder: ApiBuilder[F]
  ): HttpModule[F]
}

object WireWithLogic {
  def apply[Controller[_[_]]](implicit
      wireWithLogic: WireWithLogic[Controller]
  ): WireWithLogic[Controller] = wireWithLogic
}
