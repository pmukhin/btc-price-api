package com.tookitaki.btcpriceapi

import cats.effect.IO
import com.tookitaki.btcpriceapi.rates.Rate
import com.tookitaki.btcpriceapi.rates.persistence.RateRepository
import org.http4s.Method
import org.joda.time.DateTime
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.mockito.MockitoSugar

import org.http4s._
import org.http4s.implicits._

class RatesServiceSpec extends FunSpec with MockitoSugar with Matchers {
  describe("when request made to /<date>") {
    val request = org.http4s.Request[IO](Method.GET, new Uri(path = "/2018-06-05"))
    it("should respond with the rate for <data>") {
      val response = new RatesService(makeRepositoryForSingleDate).service
        .orNotFound(request)
        .unsafeRunSync
      response.status shouldBe Status.Ok
      response.as[String].unsafeRunSync shouldBe """{"id":24,"value":856.6,"dateLabel":"2018-06-05"}"""
    }
  }
  describe("when request made to /<dateFrom>/<dateTo>") {
    val request = org.http4s.Request[IO](Method.GET, new Uri(path = "/2018-06-05/2018-06-06"))
    it("should respond with the rate from <dateFrom> to <dateTo>") {
      val response = new RatesService(makeRepositoryForMulti).service
        .orNotFound(request)
        .unsafeRunSync
      response.status shouldBe Status.Ok
      response.as[String].unsafeRunSync shouldBe
        """[{"id":24,"value":856.6,"dateLabel":"2018-06-05"},{"id":24,"value":856.6,"dateLabel":"2018-06-06"}]"""
    }
  }
  describe("when request made to /<dateFrom>/<dateTo>") {
    describe("and when <dateFrom> is after <dateTo>") {
      val request = org.http4s.Request[IO](Method.GET, new Uri(path = "/2018-06-08/2018-06-06"))
      it("should respond with error 400") {
        val response = new RatesService(makeRepositoryForMulti).service
          .orNotFound(request)
          .unsafeRunSync
        response.status shouldBe Status.BadRequest
        response.as[String].unsafeRunSync shouldBe
          """{"error":true,"message":"dateFrom is after dateTo"}"""
      }
    }
  }

  private def makeRepositoryForMulti = new RateRepository {
    override def findByDate(dateTime: DateTime): IO[Option[Rate]] = ???

    override def findByInterval(dateTimeStart: DateTime, dateTimeEnd: DateTime): IO[Seq[Rate]] =
      IO.pure(Seq(Rate(Some(24), 856.6D, dateTimeStart), Rate(Some(24), 856.6D, dateTimeEnd)))
  }

  private def makeRepositoryForSingleDate = new RateRepository {
    override def findByDate(dateTime: DateTime): IO[Option[Rate]] = IO.pure(Some(Rate(Some(24), 856.6D, dateTime)))

    override def findByInterval(dateTimeStart: DateTime, dateTimeEnd: DateTime): IO[Seq[Rate]] = ???
  }
}
