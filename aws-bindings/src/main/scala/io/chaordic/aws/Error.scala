package io.chaordic.aws

import io.circe
import org.http4s.ParseFailure

sealed trait Error

object Error{
  implicit class AsGateWay[A](either: Either[circe.Error, A]){
    def asGw: Either[Error, A] = either.fold(a => Left(CirceError(a)), res => Right(res))
  }

  implicit class AsGateWayParseError[A](either: Either[ParseFailure, A]){
    def asGw: Either[Error, A] = either.fold(a => Left(ParseError(a)), res => Right(res))
  }
}

case class CirceError(error: circe.Error) extends Error

case class ParseError(error: ParseFailure) extends Error
