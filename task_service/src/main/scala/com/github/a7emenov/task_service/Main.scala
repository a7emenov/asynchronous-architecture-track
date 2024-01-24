package com.github.a7emenov.task_service

import cats.effect.{ExitCode, IO, IOApp}
import com.github.a7emenov.task_service.api.{Server, TaskRoutes}
import com.github.a7emenov.task_service.configuration.ApplicationConfig
import com.github.a7emenov.task_service.consumers.UserStreamingConsumer
import com.github.a7emenov.task_service.services.{AuthenticationService, TaskService, UserService}
import org.http4s.blaze.client.BlazeClientBuilder

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      config <- ApplicationConfig.load[IO]
      _ <- BlazeClientBuilder[IO].resource.use { httpClient =>
        for {
          userService <- UserService.make[IO]
          result <- TaskService.make(config.taskBusinessProducer, userService).use { taskService =>
            val authenticationService = AuthenticationService.make(config.authentication, httpClient)
            for {
              _ <- UserStreamingConsumer.stream(userService, config.userStreamingConsumer).compile.drain.start
              result <- Server.start(
                config = config.http,
                ec = runtime.compute,
                routes = new TaskRoutes[IO](authenticationService, taskService))
            } yield result
          }
        } yield result
      }
    } yield ExitCode.Success
}
