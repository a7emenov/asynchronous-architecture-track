package com.github.a7emenov.task_service.services

import cats.effect.Sync
import com.github.a7emenov.task_service.domain.{Task, TaskId, UserId}

trait TaskService[F[_]] {

  def create(description: String): F[Task]

  def listAll(userId: UserId): F[List[Task]]

  def complete(
    userId: UserId,
    taskId: TaskId): F[Option[Task]]

  def reshuffle: F[List[(UserId, Task)]]
}

object TaskService {

  def make[F[_]: Sync](userService: UserService[F]): F[TaskService[F]] =
    TaskServiceInMemoryImplementation.make(userService)
}
