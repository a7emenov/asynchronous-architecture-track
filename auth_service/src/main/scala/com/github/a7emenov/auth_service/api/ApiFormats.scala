package com.github.a7emenov.auth_service.api

import cats.syntax.either._
import com.github.a7emenov.auth_service.domain.{User, UserId, UserRole}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Codec, Decoder, Encoder}

object ApiFormats {

  implicit val configuration: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit val userRoleDecoder: Decoder[UserRole] =
    Decoder.decodeString.emap {
      case "admin"            => UserRole.Admin.asRight
      case "manager"          => UserRole.Manager.asRight
      case "regular_employee" => UserRole.RegularEmployee.asRight
      case _                  => "invalid user role".asLeft
    }

  implicit val userRoleEncoder: Encoder[UserRole] =
    Encoder.encodeString.contramap(_.name)

  implicit val userIdCodec: Codec[UserId] =
    deriveUnwrappedCodec

  implicit val userCodec: Codec[User] =
    deriveConfiguredCodec

  case class UserAuthenticationResponse(
    userId: UserId,
    firstName: String,
    lastName: String,
    role: UserRole)

  object UserAuthenticationResponse {

    implicit val encoder: Encoder[UserAuthenticationResponse] =
      deriveConfiguredEncoder
  }
}
