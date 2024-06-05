import sbt._
import sbt.librarymanagement.ModuleID

object Dependencies {
  import Jars._

  private def testDependencies: Vector[ModuleID] =
    Vector(
      testing.scalaTest
    )

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
        enumeratum.core,
        enumeratum.doobie,
        estatico.newtype,
        fs2.core,
        http4s.dsl,
        http4s.emberServer,
        profunktor.jwtAuth,
        pureconfig.core,
        scalapass.core,
        sttp.async,
        sttp.core,
        tapir.core,
        tapir.enumeratum,
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

  object Chat {
    protected def srcDependencies: Vector[ModuleID] =
      Vector(
        cats.effect,
        fs2.core,
        http4s.emberServer,
        http4s.dsl
      )

    def dependencies: Vector[ModuleID] = srcDependencies ++ testDependencies.map(_ % Test)
  }
}
