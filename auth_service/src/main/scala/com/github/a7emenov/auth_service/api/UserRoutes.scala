package com.github.a7emenov.auth_service.api

import cats.effect.Async
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.a7emenov.auth_service.api.ApiFormats._
import com.github.a7emenov.auth_service.domain.{User, UserId}
import com.github.a7emenov.auth_service.services.UserService
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDslBinCompat
import org.http4s.server.Router

class UserRoutes[F[_]: Async](userService: UserService[F]) extends Server.Routes[F] with Http4sDslBinCompat[F] {

  private def userRoutes: HttpRoutes[F] =
    HttpRoutes.of {
      case request @ PUT -> Root / "create" =>
        for {
          user <- request.as[User]
          userId <- userService.create(user)
          result <- Ok(userId)
        } yield result

      case request @ POST -> Root / "update" / userId =>
        for {
          user <- request.as[User]
          updateResult <- userService.update(UserId(userId), user)
          result <- updateResult match {
            case Some(_) =>
              Ok()

            case None =>
              NotFound()
          }
        } yield result

      case GET -> Root / "get" / userId =>
        for {
          user <- userService.get(UserId(userId))
          result <- user match {
            case Some(user) =>
              Ok(user)

            case None =>
              NotFound()
          }
        } yield result

      case DELETE -> Root / "delete" / userId =>
        userService.delete(UserId(userId)) >> Ok()
    }

  override val routes: HttpRoutes[F] =
    Router[F](
      "user" -> userRoutes
    )

}
