package io.chaordic.aws

import java.io.{InputStream, OutputStream}
import java.nio.charset.StandardCharsets

import cats.{Applicative, MonadError}
import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import org.http4s._
import fs2.{Pure, Stream, text}
import cats.implicits._

import scala.io.Source
import scala.util.Try

/**
 * This code is inspired in large part by, and borrows from https://github.com/howardjohn/scala-server-lambda.
 * However, it is re-implemented with minor differences here to account for a slightly different direction.
 */

case class ApiGatewayRequest(httpMethod: String,
                   path: String,
                   headers: Option[Map[String, String]],
                   body: Option[String],
                   queryStringParameters: Option[Map[String, String]]
                  )

case class ApiGatewayResponse(statusCode: Int, headers: Map[String, String], body: String)

case class ApiGatewayProxy[F[_]](service: HttpRoutes[F], run: F[Unit] => Unit)(implicit F: MonadError[F, Throwable],
                                                         decoder: EntityDecoder[F, String]){
  import Error._
  import ApiGatewayProxy._

  def handle(is: InputStream, os: OutputStream): F[Unit] = {
    val rawRequest = Source.fromInputStream(is).mkString
    is.close()

    val res = for{
      gwRequest <- ApiGatewayProxy.parseRequest(rawRequest).asGw
      http4sRequest <- parseRequest[F](gwRequest).asGw
    }yield{
      runRequest(service, http4sRequest)
    }

    res.fold(error => {
      F.pure({
        os.write(ApiGatewayProxy.encodeResponse(ApiGatewayResponse(500, Map.empty, error.toString)).getBytes(StandardCharsets.UTF_8))
        os.flush()
        os.close()
      })
    }, res => {
      for{
        response <- res
      }yield{
        os.write((ApiGatewayProxy.encodeResponse(response)).getBytes(StandardCharsets.UTF_8))
        os.flush()
        os.close()
      }
    })
  }
}

object ApiGatewayProxy {

  def parseRequest(rawInput: String): Either[circe.Error, ApiGatewayRequest] =
    decode[ApiGatewayRequest](rawInput)

  def encodeResponse(response: ApiGatewayResponse): String =
    response.asJson.noSpaces

  def parseRequest[F[_]](request: ApiGatewayRequest): Either[ParseFailure, Request[F]] =
    for {
      uri <- Uri.fromString(reconstructPath(request))
      method <- Method.fromString(request.httpMethod)
    } yield
      Request[F](
        method,
        uri,
        headers = request.headers.map(toHeaders).getOrElse(Headers.empty),
        body = request.body.map(encodeBody).getOrElse(EmptyBody)
      )

  def runRequest[F[_]](service: HttpRoutes[F], request: Request[F])(implicit F: MonadError[F, Throwable],
                                                                    decoder: EntityDecoder[F, String]): F[ApiGatewayResponse] =
    Try {
      service
        .run(request)
        .getOrElse(Response.notFound)
        .flatMap(asProxyResponse(_))
    }.fold(errorResponse.andThen(e => Applicative[F].pure(e)), identity(_))

  private def toHeaders(headers: Map[String, String]): Headers =
    Headers {
      headers.map {
        case (k, v) => Header(k, v)
      }.toList
    }

  private def reconstructPath(request: ApiGatewayRequest): String = {
    val requestString = request.queryStringParameters
      .map {
        _.map {
          case (k, v) => s"$k=$v"
        }.mkString("&")
      }
      .map { qs =>
        if (qs.isEmpty) "" else "?" + qs
      }
      .getOrElse("")
    request.path + requestString
  }

  private def encodeBody(body: String): Stream[Pure, Byte] = Stream(body).through(text.utf8Encode)

  private val errorResponse = (err: Throwable) => ApiGatewayResponse(500, Map.empty, err.getMessage)

  private def asProxyResponse[F[_]](resp: Response[F])(implicit F: MonadError[F, Throwable],
                                                   decoder: EntityDecoder[F, String]): F[ApiGatewayResponse] =
    resp
      .as[String]
      .map { body =>
        ApiGatewayResponse(
          resp.status.code,
          resp.headers.toList
            .map(h => h.name.value -> h.value)
            .toMap,
          body)
      }
}
