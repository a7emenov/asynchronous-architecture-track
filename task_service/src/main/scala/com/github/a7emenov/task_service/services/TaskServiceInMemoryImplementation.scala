package com.github.a7emenov.task_service.services

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import cats.syntax.traverse._
import com.github.a7emenov.task_service.domain.{Task, TaskId, TaskStatus, UserId}

import java.util.UUID
import scala.collection.mutable
import scala.util.Random

class TaskServiceInMemoryImplementation[F[_]: Sync](random: Random,
                                                    map: mutable.Map[UserId, mutable.Map[TaskId, Task]],
                                                    userService: UserService[F]) extends TaskService[F] {

  override def create(description: String): F[Task] =
    for {
      taskId <- Sync[F].delay(UUID.randomUUID().toString)
      price <- Sync[F].delay(random.between(0, 100))
      user <- userService.getRandom
      task = Task(TaskId(taskId), description, price, user, TaskStatus.InProgress)
      _ <- addTask(task)
    } yield task

  override def complete(userId: UserId, taskId: TaskId): F[Option[Task]] =
    Sync[F].delay {
      map.get(userId).flatMap(_.get(taskId)) match {
        case Some(task) =>
          val newTask = task.copy(status = TaskStatus.Completed)
          map(userId)(taskId) = newTask
          newTask.some
        case None =>
          none
      }
    }

  override def listAll(userId: UserId): F[List[Task]] =
    Sync[F].delay(map.get(userId).toList.flatMap(_.values))

  override def reshuffle: F[List[Task]] =
    Sync[F].defer {
      val allTasks = map.values.flatMap(_.values).toList
      map.clear()
      allTasks.traverse { task =>
        for {
          newUser <- userService.getRandom
          newTask = task.copy(assignedUser = newUser)
          _ <- addTask(newTask)
        } yield newTask
      }
    }


  private def addTask(task: Task): F[Unit] =
    Sync[F].delay {
      if (!map.contains(task.assignedUser.id))
        map(task.assignedUser.id) = mutable.Map.empty
      map(task.assignedUser.id)(task.taskId) = task
    }
}

object TaskServiceInMemoryImplementation {

  def make[F[_]: Sync](userService: UserService[F]): F[TaskService[F]] =
    for {
      storage <- Sync[F].delay(mutable.Map.empty[UserId, mutable.Map[TaskId, Task]])
      random <- Sync[F].delay(new Random())
      service = new TaskServiceInMemoryImplementation(random, storage, userService)
    } yield service
}
