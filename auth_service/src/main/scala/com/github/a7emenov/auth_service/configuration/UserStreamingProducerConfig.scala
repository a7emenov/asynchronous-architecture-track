package com.github.a7emenov.auth_service.configuration

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class UserStreamingProducerConfig(bootstrapServer: String,
                                       topic: String)

object UserStreamingProducerConfig {

  implicit val reader: ConfigReader[UserStreamingProducerConfig] =
    deriveReader
}
