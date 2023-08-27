package com.github.a7emenov.auth_service.api

import cats.syntax.either._
import com.github.a7emenov.auth_service.domain.UserRole
import io.circe.{Decoder, Encoder}

object ApiFormats {

  implicit val userRoleDecoder: Decoder[UserRole] =
    Decoder.decodeString.emap {
      case "admin"            => UserRole.Admin.asRight
      case "manager"          => UserRole.Manager.asRight
      case "regular_employee" => UserRole.RegularEmployee.asRight
      case _                  => "invalid user role".asLeft
    }

  implicit val userRoleEncoder: Encoder[UserRole] =
    Encoder.encodeString.contramap(_.name)
}
