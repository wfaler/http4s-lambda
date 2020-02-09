package io.chaordic.aws

import java.io.{InputStream, OutputStream}

import cats.effect.IO
import org.http4s.HttpRoutes


trait IOService {
  def routes: HttpRoutes[IO]

  def handle(is: InputStream, out: OutputStream) = new ApiGatewayProxy[IO](routes).handle(is, out).unsafeRunSync()

}
