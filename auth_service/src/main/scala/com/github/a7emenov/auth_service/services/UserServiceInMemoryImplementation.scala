package com.github.a7emenov.auth_service.services

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.a7emenov.auth_service.domain.{User, UserId}

import java.util.UUID
import scala.collection.mutable
import cats.syntax.option._
import cats.syntax.applicative._

class UserServiceInMemoryImplementation[F[_]: Sync](map: mutable.Map[UserId, User]) extends UserService[F] {

  override def create(user: User): F[UserId] =
    for {
      uuid <- Sync[F].delay(UUID.randomUUID())
      userId = UserId(uuid.toString)
      _ <- Sync[F].delay(map.update(userId, user))
    } yield userId

  override def update(
    userId: UserId,
    user: User): F[Option[Unit]] =
    for {
      userExists <- Sync[F].delay(map.contains(userId))
      result <-
        if (userExists)
          Sync[F].delay(map.update(userId, user).some)
        else
          none.pure[F]
    } yield result

  override def get(userId: UserId): F[Option[User]] =
    Sync[F].delay(map.get(userId))

  override def delete(userId: UserId): F[Unit] =
    Sync[F].delay(map.remove(userId))
}

object UserServiceInMemoryImplementation {

  def make[F[_]: Sync]: F[UserService[F]] =
    for {
      storage <- Sync[F].delay(mutable.Map.empty[UserId, User])
    } yield new UserServiceInMemoryImplementation(storage)
}
