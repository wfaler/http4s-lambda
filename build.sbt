import Dependencies._

lazy val commonSettings = Seq(
  organization := "io.chaordic.http4s-lambda",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.8"
)

lazy val awsBindings = (project in file("aws-bindings")).
 settings(
   commonSettings,
   name := "core-bindings",
   libraryDependencies ++= Seq(circeGeneric, circeParser, http4sDsl, http4sCirce,
     scalatest % Test)
 )

 lazy val example = (project in file("example")).
  settings(
    commonSettings,
    name := "example-app",
    libraryDependencies ++= Seq(
      scalatest % Test),
    assemblyJarName in assembly := "http4s-lambda.jar",
  ).dependsOn(awsBindings)
