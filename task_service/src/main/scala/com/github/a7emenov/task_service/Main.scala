package com.github.a7emenov.task_service

import cats.effect.{ExitCode, IO, IOApp}
import com.github.a7emenov.task_service.api.{Server, TaskRoutes}
import com.github.a7emenov.task_service.configuration.ApplicationConfig
import com.github.a7emenov.task_service.services.UserService

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      config <- ApplicationConfig.load[IO]
      _ <- UserService.make[IO]
      _ <- Server.start(config = config.http, ec = runtime.compute, routes = new TaskRoutes[IO]())
    } yield ExitCode.Success
}
