package com.github.a7emenov.task_service.configuration

import cats.effect.Sync
import pureconfig.error.ConfigReaderException
import pureconfig.generic.semiauto._
import pureconfig.{ConfigReader, ConfigSource}

case class ApplicationConfig(
  http: HttpConfig,
  authentication: AuthenticationConfig,
  userStreamingConsumer: UserStreamingConsumerConfig,
  taskBusinessProducer: TaskBusinessProducerConfig)

object ApplicationConfig {

  implicit val reader: ConfigReader[ApplicationConfig] =
    deriveReader

  def load[F[_]: Sync]: F[ApplicationConfig] =
    Sync[F].defer {
      Sync[F]
        .fromEither(
          ConfigSource.default
            .load[ApplicationConfig]
            .left
            .map(ConfigReaderException[ApplicationConfig](_))
        )
    }
}
