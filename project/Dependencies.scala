import sbt._
import sbt.librarymanagement.ModuleID

object Dependencies {
  import Jars._

  private def testDependencies: Vector[ModuleID] =
    Vector.empty[ModuleID]

  object Core {
    protected def srcDependencies: Vector[ModuleID] =
      Vector(
        cats.effect
      )

    def dependencies: Vector[ModuleID] = srcDependencies ++ testDependencies.map(_ % Test)
  }
}
