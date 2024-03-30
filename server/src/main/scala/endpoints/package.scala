import sttp.tapir._

package object endpoints {
  final val ApiV1: EndpointInput[Unit] = "api" / "v1"
}
