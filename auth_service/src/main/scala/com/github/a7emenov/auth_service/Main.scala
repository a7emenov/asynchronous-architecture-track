package com.github.a7emenov.auth_service

import cats.effect.{ExitCode, IO, IOApp}
import com.github.a7emenov.auth_service.api.{Server, UserRoutes}
import com.github.a7emenov.auth_service.configuration.ApplicationConfig
import com.github.a7emenov.auth_service.services.UserService

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      config <- ApplicationConfig.load[IO]
      userService <- UserService.make[IO]
      _ <- Server.start(config = config.http, ec = runtime.compute, routes = new UserRoutes(userService))
    } yield ExitCode.Success
}
