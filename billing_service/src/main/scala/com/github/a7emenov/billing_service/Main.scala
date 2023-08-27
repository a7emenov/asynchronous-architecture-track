package com.github.a7emenov.billing_service

import cats.effect.{ExitCode, IO, IOApp}
import com.github.a7emenov.billing_service.api.{BillingRoutes, Server}
import com.github.a7emenov.billing_service.configuration.ApplicationConfig
import com.github.a7emenov.billing_service.consumers.TaskBusinessConsumer
import com.github.a7emenov.billing_service.services.BalanceService

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      config <- ApplicationConfig.load[IO]
      balanceService <- BalanceService.make[IO]
      _ <- TaskBusinessConsumer.stream(balanceService, config.taskBusinessConsumer).compile.drain.start
      _ <-
        Server.start(
          config = config.http,
          ec = runtime.compute,
          routes = new BillingRoutes(balanceService)
        )

    } yield ExitCode.Success
}
