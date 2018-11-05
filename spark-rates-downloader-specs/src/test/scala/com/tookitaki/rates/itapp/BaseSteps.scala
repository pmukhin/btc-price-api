package com.tookitaki.rates.itapp

import java.io.{BufferedWriter, File, FileWriter}

import com.typesafe.config.{Config, ConfigFactory}
import cucumber.api.scala.{EN, ScalaDsl}
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords}
import org.scalatest.Matchers

import scala.util.Random
import scala.collection.mutable.ListBuffer
import scala.io.Source

class BaseSteps extends ScalaDsl with EN with Matchers {

  import sys.process._

  import scala.collection.JavaConversions._

  implicit val config: Config = ConfigFactory.load("e2e.conf")

  // unique id of the current build
  private val sessionId = Random.nextString(10)

  Given("""^I submit the spark app with url (.*)$""") { uri: String =>
    // submit the spark app from
    val response =
      s"""docker exec spark-master /spark/bin/spark-submit
         |--verbose /lib/jars/btc-price-api-assembly.jar $uri""".stripMargin.!

    response shouldBe 0
  }

  Then("""^I wait (\d+) seconds$""") { sec: Int =>
    if (sec > 20) {
      fail("that's a bit too long, it should work faster")
    }
    Thread.sleep(sec * 1000)
  }

  Then("""^I expect (\d+) messages in kafka""") { m: Int =>
    val kafkaConsumer = ConsumerFactory.create
    kafkaConsumer.subscribe(config.getStringList("kafka.topics"))

    val records = new ListBuffer[ConsumerRecord[String, String]]
    while (records.length <= m) {
      val r = kafkaConsumer.poll(1000).asInstanceOf[ConsumerRecords[String, String]]
      for (rec <- r) {
        records.append(rec)
      }
    }

    // save to file
    val file = new File(s"/tmp/$sessionId.data")
    val bw = new BufferedWriter(new FileWriter(file))
    records
      .map(r => r.value() + "\n")
      .foreach(bw.write)

    bw.close()
  }

  And("""^And messages should be like (.*)$""") { file: String =>
    val present = Source.fromFile(s"/tmp/$sessionId.data").getLines.mkString("\n")
    val expectation = Source.fromInputStream(getClass.getResourceAsStream(s"expected/$file")).getLines().mkString("")

    present shouldBe expectation
  }
}