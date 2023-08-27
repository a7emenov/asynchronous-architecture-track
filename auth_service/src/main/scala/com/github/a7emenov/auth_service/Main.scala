package com.github.a7emenov.auth_service

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    IO(ExitCode.Success)
}
