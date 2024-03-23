import Dependencies.Core

ThisBuild / libraryDependencies ++= Seq(
  compilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
  compilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
)

ThisBuild / scalaVersion := "2.13.12"

lazy val `mediator-app` = (project in file("."))
  .settings(
    name := "mediator-app",
    libraryDependencies ++= Core.dependencies
  )