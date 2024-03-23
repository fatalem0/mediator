import sbt.librarymanagement.syntax._

object Jars {
  object cats {
    private val effectVersion = "3.5.4"

    val effect = "org.typelevel" %% "cats-effect" % effectVersion
  }
}
