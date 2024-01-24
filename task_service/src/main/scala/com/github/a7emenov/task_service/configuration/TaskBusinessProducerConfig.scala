package com.github.a7emenov.task_service.configuration

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class TaskBusinessProducerConfig(bootstrapServer: String,
                                      topic: String)

object TaskBusinessProducerConfig {

  implicit val reader: ConfigReader[TaskBusinessProducerConfig] =
    deriveReader
}
