package com.github.a7emenov.auth_service.services

import cats.effect.Sync
import com.github.a7emenov.auth_service.domain.{User, UserId}

trait UserService[F[_]] {

  def create(user: User): F[UserId]

  def update(
    userId: UserId,
    user: User): F[Option[Unit]]

  def get(userId: UserId): F[Option[User]]

  def delete(userId: UserId): F[Unit]
}

object UserService {

  def make[F[_]: Sync]: F[UserService[F]] =
    UserServiceInMemoryImplementation.make
}
