package com.github.a7emenov.auth_service

import cats.effect.{ExitCode, IO, IOApp}
import com.github.a7emenov.auth_service.api.{AuthenticationRoutes, Server, UserRoutes}
import com.github.a7emenov.auth_service.configuration.ApplicationConfig
import com.github.a7emenov.auth_service.services.{AuthenticationService, UserService}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      config <- ApplicationConfig.load[IO]
      userService <- UserService.make[IO]
      authenticationService = AuthenticationService.make(config.authentication, userService)
      _ <- Server.start(
        config = config.http,
        ec = runtime.compute,
        routes = new UserRoutes(userService),
        new AuthenticationRoutes(authenticationService)
      )
    } yield ExitCode.Success
}
