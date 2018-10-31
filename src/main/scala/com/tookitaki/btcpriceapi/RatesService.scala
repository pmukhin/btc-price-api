package com.tookitaki.btcpriceapi

import cats.effect.IO
import com.tookitaki.btcpriceapi.rates.Rate
import com.tookitaki.btcpriceapi.rates.persistence.RateRepository
import org.http4s.HttpService
import org.joda.time.DateTime
import org.http4s._
import org.http4s.circe._
import io.circe.generic.auto._
import org.http4s.dsl.Http4sDsl

import scala.language.higherKinds

class RatesService(rateRepository: RateRepository)
  extends Http4sDsl[IO] {

  case class ErrorResponse(error: Boolean = true, message: String)

  import com.tookitaki.btcpriceapi.rates.Implicits._

  implicit val ioRateEncoder: EntityEncoder[IO, Rate] = jsonEncoderOf[IO, Rate]
  implicit val ioRateSeqEncoder: EntityEncoder[IO, Seq[Rate]] = jsonEncoderOf[IO, Seq[Rate]]
  implicit val ioErrorResponseEncoder: EntityEncoder[IO, ErrorResponse] = jsonEncoderOf[IO, ErrorResponse]

  val service: HttpService[IO] = HttpService[IO] {

    case GET -> Root / date =>
      val dateTime = DateTime parse date
      rateRepository
        .findByDate(dateTime)
        .flatMap(_.fold(NotFound())(Ok(_)))

    case GET -> Root / dateFrom / dateTo =>
      val dateTimeStart = DateTime parse dateFrom
      val dateTimeEnd = DateTime parse dateTo

      if (dateTimeStart.isAfter(dateTimeEnd)) {
        BadRequest(ErrorResponse(message = "dateFrom is after dateTo"))
      } else {
        for {
          rates <- rateRepository.findByInterval(dateTimeStart, dateTimeEnd)
          response <- Ok(rates)
        } yield response
      }
  }
}