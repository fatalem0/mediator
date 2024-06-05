import Dependencies.{Chat, Core}

ThisBuild / libraryDependencies ++= Seq(
  compilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
  compilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
)

ThisBuild / scalaVersion := "2.13.12"

ThisBuild / scalacOptions ++= Seq(
  "-Ymacro-annotations"
)

lazy val `mediator-app-chat` = (project in file("./chat"))
  .settings(
    name := "mediator-app-chat",
    libraryDependencies ++= Chat.dependencies
  )

lazy val `mediator-app` = (project in file("./server"))
  .settings(
    name := "mediator-app",
    libraryDependencies ++= Core.dependencies
  )

lazy val `mediator` = (project in file("."))
  .aggregate(
    `mediator-app`,
    `mediator-app-chat`
  )