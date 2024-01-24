package com.github.a7emenov.task_service.consumers

import cats.effect.Async
import cats.effect.kernel.Sync
import cats.syntax.either._
import cats.syntax.flatMap._
import com.github.a7emenov.task_service.configuration.UserStreamingConsumerConfig
import com.github.a7emenov.task_service.domain.{User, UserId, UserRole}
import com.github.a7emenov.task_service.services.UserService
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, KafkaConsumer, Deserializer => KafkaDeserializer}
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.parser.decode

object UserStreamingConsumer {

  implicit private val configuration: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit private val userRoleEncoder: Decoder[UserRole] =
    Decoder.decodeString.emap {
      case "admin"            => UserRole.Admin.asRight
      case "manager"          => UserRole.Manager.asRight
      case "regular_employee" => UserRole.RegularEmployee.asRight
      case _                  => "invalid user role".asLeft
    }

  implicit private val userIdEncoder: Decoder[UserId] =
    deriveUnwrappedDecoder

  implicit val userEncoder: Decoder[User] =
    deriveConfiguredDecoder

  implicit def userIdSerializer[F[_]: Sync]: KafkaDeserializer[F, UserId] =
    KafkaDeserializer.string.map(UserId)

  implicit def userStreamingEventSerializer[F[_]: Sync]: KafkaDeserializer[F, User] =
    KafkaDeserializer.lift(bytes => Sync[F].fromEither(decode[User](new String(bytes))))

  def stream[F[_]: Async](userService: UserService[F], config: UserStreamingConsumerConfig): fs2.Stream[F, Unit] =
    KafkaConsumer
      .stream(ConsumerSettings[F, UserId, User]
        .withBootstrapServers(config.bootstrapServer)
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withGroupId("task-service.consumer-streaming-group")
      )
      .subscribeTo(config.topic)
      .flatMap(_.stream)
      .evalMap(record  => userService.upsert(record.record.value) >> record.offset.commit)
}
