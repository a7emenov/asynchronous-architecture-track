package com.github.a7emenov.billing_service.services

import cats.effect.Sync
import com.github.a7emenov.billing_service.domain.{UserBalance, UserId}
import cats.syntax.flatMap._
import cats.syntax.functor._

import scala.collection.mutable

class BalanceServiceInMemoryImplementation[F[_]: Sync](map: mutable.Map[UserId, UserBalance]) extends BalanceService[F] {

  override def update(userId: UserId, value: BigDecimal): F[Unit] =
    Sync[F].delay(map.update(userId, UserBalance(map.getOrElse(userId, UserBalance(0)).value + value)))

  override def get(userId: UserId): F[UserBalance] =
    Sync[F].delay(map.getOrElse(userId, UserBalance(0)))
}


object BalanceServiceInMemoryImplementation {

  def make[F[_]: Sync]: F[BalanceService[F]] =
    for {
      storage <- Sync[F].delay(mutable.Map.empty[UserId, UserBalance])
      _ <- Sync[F].delay(storage.put(UserId("f1c0f431-1d64-492b-be8e-baf57bb44a12"), UserBalance(100)))
    } yield new BalanceServiceInMemoryImplementation(storage)
}
