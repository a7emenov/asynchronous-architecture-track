package com.github.a7emenov.task_service.services

import cats.effect.{Async, Resource, Sync}
import com.github.a7emenov.task_service.configuration.TaskBusinessProducerConfig
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

  def make[F[_]: Async](config: TaskBusinessProducerConfig, userService: UserService[F]): Resource[F, TaskService[F]] =
    for {
      core <- Resource.eval(TaskServiceInMemoryImplementation.make(userService))
      kafka <- TaskServiceKafkaImplementation.make(core, config)
    } yield kafka
}
