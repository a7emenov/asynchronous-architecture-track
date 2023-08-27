package com.github.a7emenov.billing_service.api

import cats.syntax.either._
import com.github.a7emenov.billing_service.domain.{User, UserId, UserRole}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Codec, Decoder, Encoder}

object ApiFormats {

  implicit val configuration: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  case class UserBalanceResponse(balance: BigDecimal)

  object UserBalanceResponse {

    implicit val encoder: Encoder[UserBalanceResponse] =
      deriveConfiguredEncoder
  }
}
