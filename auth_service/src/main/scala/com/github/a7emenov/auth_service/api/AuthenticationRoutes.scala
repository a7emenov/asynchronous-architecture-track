package com.github.a7emenov.auth_service.api

import cats.data.OptionT
import cats.effect.Async
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.a7emenov.auth_service.api.ApiFormats._
import com.github.a7emenov.auth_service.api.AuthenticationRoutes.{authenticationTokenCookieName, withAuthCookie}
import com.github.a7emenov.auth_service.domain.{AuthenticationToken, UserId}
import com.github.a7emenov.auth_service.services.AuthenticationService
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDslBinCompat
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Request, Response, ResponseCookie}

class AuthenticationRoutes[F[_]: Async](authenticationService: AuthenticationService[F])
    extends Server.Routes[F] with Http4sDslBinCompat[F] {

  private def userRoutes: HttpRoutes[F] =
    HttpRoutes.of {
      case POST -> Root / "login" / userId =>
        for {
          token <- authenticationService.login(UserId(userId))
          result <- token match {
            case Some(t) =>
              Ok().map(withAuthCookie(_, t))

            case None =>
              NotFound()
          }
        } yield result

      case request @ POST -> Root / "authenticate" =>
        for {
          response <- authenticate(request).value
          result <- response match {
            case Some(r) => Ok(r)
            case None    => Forbidden()
          }
        } yield result
    }

  private def authenticate(request: Request[F]) =
    for {
      cookie <- OptionT.fromOption(request.cookies.find(_.name == authenticationTokenCookieName))
      token = AuthenticationToken(cookie.content)
      (userId, user) <- OptionT(authenticationService.authenticate(token))
    } yield UserAuthenticationResponse(userId, user.firstName, user.lastName, user.role)

  override val routes: HttpRoutes[F] =
    Router[F](
      "authentication" -> userRoutes
    )

}

object AuthenticationRoutes {

  private val authenticationTokenCookieName = "AATSESSIONID"

  private def withAuthCookie[F[_]](
    response: Response[F],
    token: AuthenticationToken): Response[F] =
    response.addCookie(
      ResponseCookie(
        name = authenticationTokenCookieName,
        content = token.value,
        path = Some("/"),
        httpOnly = true
      )
    )
}
