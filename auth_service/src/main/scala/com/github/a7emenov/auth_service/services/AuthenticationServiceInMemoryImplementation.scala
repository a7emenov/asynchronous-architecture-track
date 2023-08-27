package com.github.a7emenov.auth_service.services

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import cats.syntax.functor._
import com.github.a7emenov.auth_service.configuration.AuthenticationConfig
import com.github.a7emenov.auth_service.domain.{AuthenticationToken, User, UserId, UserRole}
import com.github.a7emenov.auth_service.services.AuthenticationServiceInMemoryImplementation._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.{Codec, Decoder, Encoder}
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}

class AuthenticationServiceInMemoryImplementation[F[_]: Applicative](
  config: AuthenticationConfig,
  userService: UserService[F])
    extends AuthenticationService[F] {

  override def login(userId: UserId): F[Option[AuthenticationToken]] =
    for {
      user <- userService.get(userId)
      token = user.map { u =>
        val body = JwtClaimBody(userId, u)
        val claim = JwtClaim(
          content = body.asJson.noSpaces
        )
        AuthenticationToken(JwtCirce.encode(claim, config.secretKey, AuthenticationServiceInMemoryImplementation.algo))
      }
    } yield token

  override def authenticate(token: AuthenticationToken): F[Option[(UserId, User)]] =
    (for {
      claim <- JwtCirce
        .decode(token.value, config.secretKey, algorithms = Seq(AuthenticationServiceInMemoryImplementation.algo))
        .toOption
      body <- decode[JwtClaimBody](claim.content).toOption
    } yield (body.userId, body.user)).pure[F]
}

object AuthenticationServiceInMemoryImplementation {

  implicit private val configuration: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  private val algo = JwtAlgorithm.HS256

  implicit private val userRoleDecoder: Decoder[UserRole] =
    Decoder.decodeString.emap {
      case "admin"            => UserRole.Admin.asRight
      case "manager"          => UserRole.Manager.asRight
      case "regular_employee" => UserRole.RegularEmployee.asRight
      case _                  => "invalid user role".asLeft
    }

  implicit private val userRoleEncoder: Encoder[UserRole] =
    Encoder.encodeString.contramap(_.name)

  implicit private val userIdEncoder: Encoder[UserId] =
    deriveUnwrappedEncoder

  implicit private val userIdDecoder: Decoder[UserId] =
    deriveUnwrappedDecoder

  implicit private val userCodec: Codec[User] =
    deriveConfiguredCodec

  private case class JwtClaimBody(
    userId: UserId,
    user: User)

  private object JwtClaimBody {

    implicit val codec: Codec[JwtClaimBody] =
      deriveConfiguredCodec
  }

  def make[F[_]: Applicative](
    config: AuthenticationConfig,
    userService: UserService[F]): AuthenticationService[F] =
    new AuthenticationServiceInMemoryImplementation(config, userService)
}
