package com.github.a7emenov.billing_service.api

import cats.effect.Async
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.a7emenov.billing_service.api.ApiFormats._
import com.github.a7emenov.billing_service.domain.UserId
import com.github.a7emenov.billing_service.services.BalanceService
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDslBinCompat
import org.http4s.server.Router

class BillingRoutes[F[_]: Async](balanceService: BalanceService[F]) extends Server.Routes[F] with Http4sDslBinCompat[F] {

  private def userRoutes: HttpRoutes[F] =
    HttpRoutes.of {
      case GET -> Root / "balance" / userId =>
        for {
          balance <- balanceService.get(UserId(userId))
          result <- Ok(UserBalanceResponse(balance.value))
        } yield result
    }

  override val routes: HttpRoutes[F] =
    Router[F](
      "billing" -> userRoutes
    )
}
