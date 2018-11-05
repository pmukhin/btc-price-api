package com.tookitaki.btcpriceapi.rates

import io.circe.{Encoder, Json}
import org.joda.time.DateTime
import slick.lifted.Tag
import slick.jdbc.H2Profile.api._

object Implicits {
  implicit val rateEncoder: Encoder[Rate] = new Encoder[Rate] {
    override def apply(r: Rate): Json = Json.obj(
      ("id", r.id.map(Json.fromInt).getOrElse(Json.Null)),
      ("value", Json.fromDouble(r.value).getOrElse(Json.Null)),
      ("dateLabel", Json.fromString(r.dateLabel.toString))
    )
  }
}

case class Rate(id: Option[Int], value: Double, dateLabel: DateTime)

class RateTable(tag: Tag) extends Table[Rate](tag, "rates") {

  import com.github.tototoshi.slick.H2JodaSupport._

  def id =
    column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)

  def value =
    column[Double]("value")

  def dateLabel =
    column[DateTime]("dateLabel")

  def * =
    (id, value, dateLabel) <> (Rate.tupled, Rate.unapply)
}