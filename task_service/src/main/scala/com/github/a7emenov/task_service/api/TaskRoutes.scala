package com.github.a7emenov.task_service.api

import cats.effect.Async
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.a7emenov.task_service.api.ApiFormats._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDslBinCompat
import org.http4s.server.Router

class TaskRoutes[F[_]: Async]() extends Server.Routes[F] with Http4sDslBinCompat[F] {

  private def taskRoutes: HttpRoutes[F] =
    HttpRoutes.empty

  override val routes: HttpRoutes[F] =
    Router[F](
      "task" -> taskRoutes
    )

}
