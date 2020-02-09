package io.chaordic.http4s

import java.io.{InputStream, OutputStream}

import cats.effect.IO
import io.chaordic.aws.ApiGatewayProxy
import org.http4s.HttpRoutes
import org.http4s.dsl.io._


class Service {

  object Route {
    val service = HttpRoutes.of[IO] {
      case GET -> Root / "hello" / name => Ok(s"Hello, $name!")
      case PUT -> Root / "hello" / name => Ok(s"Hello PUT, $name!")
      case PUT -> Root / "bye" / "bye" => Ok(s"Bye root")
    }
  }

  def handle(is:InputStream, os: OutputStream) = {
    new ApiGatewayProxy[IO](Route.service, (x) => x.unsafeRunSync()).handle(is, os).unsafeRunSync()
  }

}
