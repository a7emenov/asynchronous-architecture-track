package com.github.a7emenov.task_service.services

import cats.effect.Sync
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import com.github.a7emenov.task_service.domain.{User, UserId, UserRole}

import java.util.UUID
import scala.collection.mutable

class UserServiceInMemoryImplementation[F[_]: Sync](map: mutable.Map[UserId, User]) extends UserService[F] {

  override def upsert(user: User): F[Unit] =
    Sync[F].delay(map.update(user.id, user).some)

  override def get(userId: UserId): F[Option[User]] =
    Sync[F].delay(map.get(userId))
}

object UserServiceInMemoryImplementation {

  def make[F[_]: Sync]: F[UserService[F]] =
    for {
      storage <- Sync[F].delay(mutable.Map.empty[UserId, User])
      service = new UserServiceInMemoryImplementation(storage)
      _ <- service.upsert(User(UserId("f1c0f431-1d64-492b-be8e-baf57bb44a12"), "Dummy", "Dummy", UserRole.Admin))
    } yield service
}
