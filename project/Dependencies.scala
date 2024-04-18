import sbt._
import sbt.librarymanagement.ModuleID

object Dependencies {
  import Jars._

  private def testDependencies: Vector[ModuleID] =
    Vector.empty[ModuleID]

  object Core {
    protected def srcDependencies: Vector[ModuleID] =
      Vector(
        cats.effect,
        chimney.core,
        derevo.cats,
        derevo.catsTagless,
        derevo.pureconfig,
        derevo.tethys,
        doobie.core,
        doobie.hikari,
        doobie.postgres,
        estatico.newtype,
        http4s.dsl,
        http4s.emberServer,
        profunktor.jwtAuth,
        pureconfig.core,
        scalapass.core,
        sttp.async,
        sttp.core,
        tapir.core,
        tapir.http4s,
        tapir.tethys,
        tethys.core,
        tethys.enumeratum,
        tofu.cats,
        tofu.layout,
        tofu.logging
      )

    def dependencies: Vector[ModuleID] = srcDependencies ++ testDependencies.map(_ % Test)
  }
}
