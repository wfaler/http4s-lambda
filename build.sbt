import Dependencies._

//import bintray.BintrayKeys._

lazy val commonSettings = Seq(
  organization := "io.chaordic",
  version := "0.0.1",
  scalaVersion := "2.12.10",
  bintrayOmitLicense := true
)

lazy val awsBindings = (project in file("aws-bindings")).
 settings(
   commonSettings,
   name := "http4s-aws-core",
   libraryDependencies ++= Seq(circeGeneric, circeParser, http4sDsl, http4sCirce,
     scalatest % Test)
 )

 lazy val example = (project in file("example")).
  settings(
    commonSettings,
    name := "example-app",
    skip in publish := true,
    libraryDependencies ++= Seq(
      scalatest % Test),
    assemblyJarName in assembly := "http4s-lambda.jar",
  ).dependsOn(awsBindings)
