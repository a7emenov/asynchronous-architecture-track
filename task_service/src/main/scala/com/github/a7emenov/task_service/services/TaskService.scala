package com.github.a7emenov.task_service.services

import com.github.a7emenov.task_service.domain.{Task, TaskId, User, UserId}

trait TaskService[F[_]] {

  def create(description: String): F[Task]

  def listAll(userId: UserId): F[List[Task]]

  def complete(userId: UserId, taskId: TaskId): F[Option[Task]]

  def reshuffle: F[List[Task]]
}
