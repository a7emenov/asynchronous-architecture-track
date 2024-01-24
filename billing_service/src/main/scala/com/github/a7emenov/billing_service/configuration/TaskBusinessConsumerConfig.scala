package com.github.a7emenov.billing_service.configuration

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class TaskBusinessConsumerConfig(bootstrapServer: String,
                                      topic: String)

object TaskBusinessConsumerConfig {

  implicit val reader: ConfigReader[TaskBusinessConsumerConfig] =
    deriveReader
}


