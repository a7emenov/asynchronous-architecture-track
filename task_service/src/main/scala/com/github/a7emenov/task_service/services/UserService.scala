package com.github.a7emenov.task_service.services

import cats.effect.Sync
import com.github.a7emenov.task_service.domain.{User, UserId}

trait UserService[F[_]] {

  def upsert(
    user: User): F[Unit]

  def get(userId: UserId): F[Option[User]]
}

object UserService {

  def make[F[_]: Sync]: F[UserService[F]] =
    UserServiceInMemoryImplementation.make
}
