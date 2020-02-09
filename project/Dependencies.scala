import sbt._

object Dependencies {
  val Http4sVersion = "0.20.0"
  val CirceVersion = "0.11.1"
  //test libs
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.8"
  val blazeServer = "org.http4s" %% "http4s-blaze-server" % Http4sVersion
  val blazeClient = "org.http4s" %% "http4s-blaze-client" % Http4sVersion
  val http4sCirce = "org.http4s" %% "http4s-circe" % Http4sVersion
  val http4sDsl = "org.http4s" %% "http4s-dsl" % Http4sVersion

  val circeJava = "io.circe" %% "circe-java8" % CirceVersion
  val circeGeneric = "io.circe" %% "circe-generic" % CirceVersion
  val circeParser = "io.circe" %% "circe-parser" % CirceVersion

//  val jline = "org.jline" % "jline" % "3.9.0"
//  val catsEffects = "org.typelevel" %% "cats-effect" % "2.0.0"
//  val parserCombinators = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"

}
