package com.github.a7emenov.task_service.services

import cats.effect.Concurrent
import cats.syntax.either._
import com.github.a7emenov.task_service.configuration.AuthenticationConfig
import com.github.a7emenov.task_service.domain.{AuthenticationToken, User, UserId, UserRole}
import com.github.a7emenov.task_service.services.AuthenticationServiceImplementation._
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveUnwrappedDecoder}
import org.http4s.Method.POST
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl

class AuthenticationServiceImplementation[F[_]: Concurrent](
  uri: Uri,
  client: Client[F])
    extends AuthenticationService[F] with Http4sClientDsl[F] {

  override def authenticate(token: AuthenticationToken): F[Option[User]] = {
    val request = POST(uri / "authentication" / "authenticate").addCookie(authenticationTokenCookieName, token.value)
    client.expectOption[User](request)
  }

}

object AuthenticationServiceImplementation {

  implicit val configuration: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  def make[F[_]: Concurrent](
    config: AuthenticationConfig,
    client: Client[F]) =
    new AuthenticationServiceImplementation[F](
      Uri.fromString(s"http://${config.authenticationServiceHost}:${config.authenticationServicePort}").toOption.get,
      client
    )

  implicit private val userRoleDecoder: Decoder[UserRole] =
    Decoder.decodeString.emap {
      case "admin"            => UserRole.Admin.asRight
      case "manager"          => UserRole.Manager.asRight
      case "regular_employee" => UserRole.RegularEmployee.asRight
      case _                  => "invalid user role".asLeft
    }

  implicit val userIdCodec: Decoder[UserId] =
    deriveUnwrappedDecoder

  implicit val userCodec: Decoder[User] =
    deriveConfiguredDecoder

  private val authenticationTokenCookieName = "AATSESSIONID"
}
