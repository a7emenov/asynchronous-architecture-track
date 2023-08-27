package com.github.a7emenov.auth_service.services

import cats.Applicative
import com.github.a7emenov.auth_service.configuration.AuthenticationConfig
import com.github.a7emenov.auth_service.domain.{AuthenticationToken, User, UserId}

trait AuthenticationService[F[_]] {

  def login(userId: UserId): F[Option[AuthenticationToken]]

  def authenticate(token: AuthenticationToken): F[Option[(UserId, User)]]
}

object AuthenticationService {

  def make[F[_]: Applicative](
    config: AuthenticationConfig,
    userService: UserService[F]): AuthenticationService[F] =
    AuthenticationServiceInMemoryImplementation.make(config, userService)
}
