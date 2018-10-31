package com.tookitaki.btcpriceapi

import java.util.concurrent.Executors

import cats.effect.IO
import com.tookitaki.btcpriceapi.rates.persistence.RateRepositoryImpl
import com.typesafe.config.{Config, ConfigFactory}
import fs2.StreamApp
import org.http4s.server.blaze.BlazeBuilder
import slick.basic.DatabaseConfig
import slick.jdbc.MySQLProfile
import slick.util.Logging

import scala.reflect.internal.util.Collections

object ServerApp extends StreamApp[IO] with Logging {

  import scala.concurrent.ExecutionContext.Implicits.global

  /** application config  */
  implicit val config: Config = ConfigFactory.load("application.conf")

  /** consumer to read changes from spark  */
  private val kafkaConsumer = ConsumerFactory.create

  private def startConsumer(): Unit = {
    kafkaConsumer.subscribe(config.getStringList("kafka.topics"))

    Executors.newSingleThreadExecutor.execute(new Runnable {
      override def run(): Unit = {
        while (true) {
          val records = kafkaConsumer.poll(1000)

          for (record <- records) {
            // logic to unjsonify record and push it to db
          }
        }
      }
    })
  }

  def stream(args: List[String], requestShutdown: IO[Unit]): fs2.Stream[IO, StreamApp.ExitCode] = {

    startConsumer()

    val database = DatabaseConfig.forConfig[MySQLProfile]("database", config)
    val rates = new RateRepositoryImpl(database)

    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(new RatesService(rates).service, "/rate/")
      .serve
  }
}
