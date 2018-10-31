package com.tookitaki.btcpriceapi.rates.persistence

import cats.effect.IO
import com.tookitaki.btcpriceapi.rates.{Rate, RateTable}
import org.joda.time.DateTime
import slick.basic.DatabaseConfig
import slick.jdbc.MySQLProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

trait RateRepository {
  def findByDate(dateTime: DateTime): IO[Option[Rate]]

  def findByInterval(dateTimeStart: DateTime, dateTimeEnd: DateTime): IO[Seq[Rate]]
}

class RateRepositoryImpl(dbConfig: DatabaseConfig[MySQLProfile])(implicit val ec: ExecutionContext)
  extends RateRepository {

  import com.github.tototoshi.slick.H2JodaSupport._

  private val rates = TableQuery[RateTable]

  import dbConfig._
  import profile.api._

  def findByDate(dateTime: DateTime): IO[Option[Rate]] = IO.fromFuture(IO {
    db.run {
      rates.filter(_.dateLabel === dateTime).result.headOption
    }
  })

  def findByInterval(dateTimeStart: DateTime, dateTimeEnd: DateTime): IO[Seq[Rate]] = IO.fromFuture(
    IO {
      db.run {
        rates
          .filter(_.dateLabel >= dateTimeStart)
          .filter(_.dateLabel <= dateTimeEnd)
          .result
      }
    }
  )
}