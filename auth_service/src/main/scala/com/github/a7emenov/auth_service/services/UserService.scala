package com.github.a7emenov.auth_service.services

import cats.effect.kernel.Async
import cats.effect.{Resource, Sync}
import com.github.a7emenov.auth_service.configuration.UserStreamingProducerConfig
import com.github.a7emenov.auth_service.domain.{User, UserId}

trait UserService[F[_]] {

  def create(user: User): F[UserId]

  def update(
    userId: UserId,
    user: User): F[Option[Unit]]

  def get(userId: UserId): F[Option[User]]
}

object UserService {

  def make[F[_]: Async](config: UserStreamingProducerConfig): Resource[F, UserService[F]] =
    for {
      core <- Resource.eval(UserServiceInMemoryImplementation.make)
      kafka <- UserServiceKafkaImplementation.make(core, config)
    } yield kafka
}
