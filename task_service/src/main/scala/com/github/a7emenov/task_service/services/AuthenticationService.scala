package com.github.a7emenov.task_service.services

import cats.effect.kernel.Concurrent
import com.github.a7emenov.task_service.configuration.AuthenticationConfig
import com.github.a7emenov.task_service.domain.{AuthenticationToken, User}
import org.http4s.client.Client

trait AuthenticationService[F[_]] {

  def authenticate(token: AuthenticationToken): F[Option[User]]
}

object AuthenticationService {

  def make[F[_]: Concurrent](
    config: AuthenticationConfig,
    client: Client[F]): AuthenticationService[F] =
    AuthenticationServiceImplementation.make(config, client)
}
