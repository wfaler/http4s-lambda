package io.chaordic.http4s

import cats.effect.IO
import io.chaordic.aws.IOService
import org.http4s.HttpRoutes
import org.http4s.dsl.io._


class LambdaHandler extends IOService{

  val routes = HttpRoutes.of[IO] {
      case GET -> Root / "hello" / name => Ok(s"Hello, $name!")
      case PUT -> Root / "hello" / name => Ok(s"Hello PUT, $name!")
      case PUT -> Root / "bye" / "bye" => Ok(s"Bye root")
    }
}
