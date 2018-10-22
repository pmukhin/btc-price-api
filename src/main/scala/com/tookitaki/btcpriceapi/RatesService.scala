package com.tookitaki.btcpriceapi

import cats.effect.Effect
import io.circe.Json
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class RatesService[IO[_] : Effect] extends Http4sDsl[IO] {

  val service: HttpService[IO] = {
    HttpService[IO] {
      case GET -> Root / "hello" / name =>
        Ok(Json.obj("message" -> Json.fromString(s"Hello, ${name}")))
    }
  }
}
