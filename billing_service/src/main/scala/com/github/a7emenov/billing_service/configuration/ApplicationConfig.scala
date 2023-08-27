package com.github.a7emenov.billing_service.configuration

import cats.effect.Sync
import pureconfig.error.ConfigReaderException
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.semiauto._

case class ApplicationConfig(
  http: HttpConfig,
  authentication: AuthenticationConfig,
  userStreamingProducer: UserStreamingProducerConfig)

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