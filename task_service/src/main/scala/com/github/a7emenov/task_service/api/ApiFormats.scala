package com.github.a7emenov.task_service.api

import cats.syntax.either._
import com.github.a7emenov.task_service.domain.{Task, TaskId, TaskStatus, User, UserId, UserRole}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{
  deriveConfiguredCodec,
  deriveConfiguredDecoder,
  deriveConfiguredEncoder,
  deriveUnwrappedCodec
}
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

  implicit val taskStatusEncoder: Encoder[TaskStatus] =
    Encoder.encodeString.contramap(_.name)

  implicit val taskIdEncoder: Encoder[TaskId] =
    Encoder.encodeString.contramap(_.value)

  implicit val userIdCodec: Codec[UserId] =
    deriveUnwrappedCodec

  implicit val userCodec: Codec[User] =
    deriveConfiguredCodec

  case class CreateTaskRequest(description: String)

  object CreateTaskRequest {

    implicit val decoder: Decoder[CreateTaskRequest] =
      deriveConfiguredDecoder
  }

  implicit val taskEncoder: Encoder[Task] =
    deriveConfiguredEncoder
}
