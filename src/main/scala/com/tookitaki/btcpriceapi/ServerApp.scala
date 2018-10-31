package com.tookitaki.btcpriceapi

import cats.effect.IO
import com.tookitaki.btcpriceapi.rates.persistence.RateRepositoryImpl
import com.typesafe.config.ConfigFactory
import fs2.StreamApp
import org.http4s.server.blaze.BlazeBuilder
import slick.basic.DatabaseConfig
import slick.jdbc.MySQLProfile
import slick.util.Logging

object ServerApp extends StreamApp[IO] with Logging {

  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], requestShutdown: IO[Unit]): fs2.Stream[IO, StreamApp.ExitCode] = {
    val config = ConfigFactory.load("application.conf")
    val database = DatabaseConfig.forConfig[MySQLProfile]("database", config)
    val rates = new RateRepositoryImpl(database)

    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(new RatesService(rates).service, "/rate/")
      .serve
  }
}
