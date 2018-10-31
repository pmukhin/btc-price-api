package com.tookitaki.rates

import org.apache.spark.sql.SparkSession

object RatesApp extends App {
  val spark = SparkSession.builder.appName("rates-downloader")
  val dataLink = ""
}
