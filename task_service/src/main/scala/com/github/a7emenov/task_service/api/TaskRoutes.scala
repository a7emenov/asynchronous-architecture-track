package com.github.a7emenov.task_service.api

import cats.data.{Kleisli, OptionT}
import cats.effect.Async
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.a7emenov.task_service.api.ApiFormats._
import com.github.a7emenov.task_service.api.TaskRoutes.authenticationTokenCookieName
import com.github.a7emenov.task_service.domain.{AuthenticationToken, TaskId, User}
import com.github.a7emenov.task_service.services.{AuthenticationService, TaskService}
import org.http4s.dsl.Http4sDslBinCompat
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.{AuthedRoutes, HttpRoutes, Request}

class TaskRoutes[F[_]: Async](
  authenticationService: AuthenticationService[F],
  taskService: TaskService[F])
    extends Server.Routes[F] with Http4sDslBinCompat[F] {

  private val middleware: AuthMiddleware[F, User] =
    AuthMiddleware.withFallThrough(authUser = Kleisli(authenticate))

  private def taskRoutes: AuthedRoutes[User, F] =
    AuthedRoutes.of {
      case request @ PUT -> Root / "create" as _ =>
        for {
          req <- request.req.as[CreateTaskRequest]
          task <- taskService.create(req.description)
          result <- Created(task)
        } yield result

      case POST -> Root / "complete" / taskId as user =>
        taskService
          .complete(user.userId, TaskId(taskId))
          .flatMap(Ok(_))

      case POST -> Root / "list" as user =>
        taskService
          .listAll(user.userId)
          .flatMap(Ok(_))

      case POST -> Root / "reshuffle" as user =>
        taskService.reshuffle >> Ok()
    }

  override val routes: HttpRoutes[F] =
    Router[F](
      "task" -> middleware(taskRoutes)
    )

  private def authenticate(request: Request[F]) =
    for {
      cookie <- OptionT.fromOption(request.cookies.find(_.name == authenticationTokenCookieName))
      token = AuthenticationToken(cookie.content)
      user <- OptionT(authenticationService.authenticate(token))
    } yield user

}

object TaskRoutes {

  private val authenticationTokenCookieName = "AATSESSIONID"
}
