package com.github.a7emenov.auth_service.services

import cats.Monad
import cats.effect.kernel.Sync
import cats.effect.{Async, Resource}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import com.github.a7emenov.auth_service.configuration.UserStreamingProducerConfig
import com.github.a7emenov.auth_service.domain.{User, UserId, UserRole}
import com.github.a7emenov.auth_service.services.UserServiceKafkaImplementation.UserStreamingEvent
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerSettings, Serializer => KafkaSerializer}
import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.syntax._

class UserServiceKafkaImplementation[F[_]: Monad](underlying: UserService[F], config: UserStreamingProducerConfig, producer: KafkaProducer[F, UserId, UserStreamingEvent]) extends UserService[F] {

  override def create(user: User): F[UserId] =
    for {
      userId <- underlying.create(user)
      _ <- producer.produceOne(ProducerRecord(
        config.topic,
        userId,
        UserStreamingEvent(userId, user.firstName, user.lastName, user.role)
      ))
    } yield userId


  override def get(userId: UserId): F[Option[User]] =
    underlying.get(userId)

  override def update(userId: UserId, user: User): F[Option[Unit]] =
    for {
      result <- underlying.update(userId, user)
      _ <- result.traverse(_ => producer.produceOne(ProducerRecord(
        config.topic,
        userId,
        UserStreamingEvent(userId, user.firstName, user.lastName, user.role)
      )))
    } yield result
}

object UserServiceKafkaImplementation {

  implicit private val configuration: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit private val userRoleEncoder: Encoder[UserRole] =
    Encoder.encodeString.contramap(_.name)

  implicit private val userIdEncoder: Encoder[UserId] =
    deriveUnwrappedEncoder

  implicit val userEncoder: Encoder[User] =
    deriveConfiguredEncoder

  case class UserStreamingEvent(
                                         userId: UserId,
                                         firstName: String,
                                         lastName: String,
                                         role: UserRole)

  object UserStreamingEvent {

    implicit val encoder: Encoder[UserStreamingEvent] =
      deriveConfiguredEncoder
  }

  implicit def userIdSerializer[F[_]: Sync]: KafkaSerializer[F, UserId] =
    KafkaSerializer.string.contramap(_.value.asJson.noSpaces)

  implicit def userStreamingEventSerializer[F[_]: Sync]: KafkaSerializer[F, UserStreamingEvent] =
    KafkaSerializer.string.contramap(_.asJson.noSpaces)

  def make[F[_]: Async](underlying: UserService[F], config: UserStreamingProducerConfig): Resource[F, UserService[F]] =
    KafkaProducer
      .resource(ProducerSettings[F, UserId, UserStreamingEvent].withBootstrapServers(config.bootstrapServer))
      .map(new UserServiceKafkaImplementation(underlying, config, _))
}
