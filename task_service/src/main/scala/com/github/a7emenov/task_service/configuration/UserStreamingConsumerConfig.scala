package com.github.a7emenov.task_service.configuration

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class UserStreamingConsumerConfig(bootstrapServer: String,
                                       topic: String)

object UserStreamingConsumerConfig {

  implicit val reader: ConfigReader[UserStreamingConsumerConfig] =
    deriveReader
}
