package com.github.a7emenov.billing_service.services

import cats.effect.Sync
import com.github.a7emenov.billing_service.domain.{UserBalance, UserId}

trait BalanceService[F[_]] {

  def update(userId: UserId, value: BigDecimal): F[Unit]

  def get(userId: UserId): F[UserBalance]
}

object BalanceService {

  def make[F[_]: Sync]: F[BalanceService[F]] =
    BalanceServiceInMemoryImplementation.make
}
