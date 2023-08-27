package com.github.a7emenov.task_service.services

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import com.github.a7emenov.task_service.domain.{User, UserId, UserRole}

import scala.collection.mutable
import scala.util.Random

class UserServiceInMemoryImplementation[F[_]: Sync](
  random: Random,
  map: mutable.Map[UserId, User])
    extends UserService[F] {

  override def upsert(user: User): F[Unit] =
    Sync[F].delay(map.update(user.userId, user).some)

  override def get(userId: UserId): F[Option[User]] =
    Sync[F].delay(map.get(userId))

  override def getRandom: F[User] =
    Sync[F].delay {
      val index = random.between(0, map.size)
      map(map.keys.toIndexedSeq(index))
    }
}

object UserServiceInMemoryImplementation {

  def make[F[_]: Sync]: F[UserService[F]] =
    for {
      storage <- Sync[F].delay(mutable.Map.empty[UserId, User])
      random <- Sync[F].delay(new Random())
      service = new UserServiceInMemoryImplementation(random, storage)
      _ <- service.upsert(User(UserId("f1c0f431-1d64-492b-be8e-baf57bb44a12"), "Dummy", "Dummy", UserRole.Admin))
    } yield service
}
