package com.tookitaki.rates

import io.circe._
import io.circe.parser.decode
import org.apache.spark.sql.SparkSession
import org.joda.time.DateTime
import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object RatesApp extends App {

  case class RawRate(value: String, date: String)

  implicit val ioRateDecoder: Decoder[RawRate] = Decoder
    .forProduct2("price", "time")(RawRate.apply)
  implicit val ioRateListDecoder: Decoder[List[RawRate]] = Decoder[List[RawRate]]
    .prepare(_.downField("data").downField("prices"))
  implicit val dateTimeEncoder: Encoder[DateTime] = (a: DateTime) =>
    Json.fromString(a.formatted("yyyy-MM-dd"))
  implicit val tupleEncoder: Encoder[(Double, DateTime)] = Encoder
    .forProduct2("value", "price")(r => (r._1, r._2))

  val props = new Properties()
  props.put("bootstrap.servers", args(2))
  props.put("client.id", "ScalaProducerExample")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  val producer = new KafkaProducer[String, String](props)

  val spark = SparkSession.builder.appName("rates-downloader").getOrCreate
  val dataLink = "https://www.coinbase.com/api/v2/prices/BTC-USD/historic?period=year"
  val data = scala.io.Source.fromURL(dataLink).getLines().mkString("")
  val prices: List[RawRate] = decode[List[RawRate]](data) match {
    case Left(e) => spark.stop; throw e
    case Right(d) => d
  }

  val pricesRdd = spark.sparkContext.parallelize(prices, 6)
  pricesRdd
    .map(r => (r.value.toDouble, DateTime.parse(r.date)))
    .map(r => tupleEncoder.apply(r))
    .collect
    .map(d => new ProducerRecord[String, String]("rates", d.toString()))
    .foreach(producer.send)
}
