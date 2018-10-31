package com.tookitaki.btcpriceapi

import java.util.Properties

import com.typesafe.config.Config
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}

object ConsumerFactory {
  def create()(implicit config: Config): KafkaConsumer[String, String] =
    createConsumer(createProps)

  private def createConsumer(props: Properties): KafkaConsumer[String, String] =
    new KafkaConsumer[String, String](props)

  private def createProps(implicit config: Config): Properties = {
    val props = new Properties()

    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getList("kafka.brokers"))
    props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getString("kafka.group-id"))
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props
  }
}
