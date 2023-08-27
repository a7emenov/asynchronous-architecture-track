package com.github.a7emenov.task_service.api

import cats.Monad
import cats.effect.Async
import cats.syntax.semigroupk._
import com.github.a7emenov.task_service.configuration.HttpConfig
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.{HttpApp, HttpRoutes}

import scala.concurrent.ExecutionContext

object Server {

  trait Routes[F[_]] {

    def routes: HttpRoutes[F]
  }

  def start[F[_]: Async](
    config: HttpConfig,
    ec: ExecutionContext,
    routes: Server.Routes[F]*): F[Unit] =
    BlazeServerBuilder[F]
      .withExecutionContext(ec)
      .withHttpApp(httpApp(routes: _*))
      .bindHttp(port = config.port, host = config.host)
      .serve
      .compile
      .drain

  private def httpApp[F[_]: Monad](routes: Server.Routes[F]*): HttpApp[F] =
    routes
      .foldLeft[HttpRoutes[F]](HttpRoutes.empty[F])((rs1, rs2) => rs1 <+> rs2.routes)
      .orNotFound
}
