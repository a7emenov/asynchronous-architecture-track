package com.github.a7emenov.task_service.services

import cats.Monad
import cats.effect.kernel.Sync
import cats.effect.{Async, Resource}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicative._
import cats.syntax.traverse._
import com.github.a7emenov.task_service.configuration.TaskBusinessProducerConfig
import com.github.a7emenov.task_service.domain.{Task, TaskId, UserId}
import com.github.a7emenov.task_service.services.TaskServiceKafkaImplementation.{TaskAssignedEvent, TaskBusinessEvent, TaskCompletedEvent, TaskUnassignedEvent}
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerResult, ProducerSettings, Serializer => KafkaSerializer}
import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.syntax._

class TaskServiceKafkaImplementation[F[_]: Monad](underlying: TaskService[F], config: TaskBusinessProducerConfig, producer: KafkaProducer[F, TaskId, TaskBusinessEvent]) extends TaskService[F] {

  override def create(description: String): F[Task] =
    for {
      result <- underlying.create(description)
      _ <- sendEvent(result.taskId, TaskAssignedEvent(result.assignedUser.userId, result.price))
    } yield result

  override def complete(
    userId: UserId,
    taskId: TaskId
                       ): F[Option[Task]] =
    for {
      result <- underlying.complete(userId, taskId)
      _ <- result.traverse(task => sendEvent(task.taskId, TaskCompletedEvent(task.assignedUser.userId, task.price)))
    } yield result


  override def listAll(userId: UserId): F[List[Task]] =
    underlying.listAll(userId)

  override def reshuffle: F[List[(UserId, Task)]] =
    for {
      result <- underlying.reshuffle
      _ <- result.traverse { case (previousUserId, task) =>
        if (previousUserId != task.assignedUser.userId)
          sendEvent(task.taskId, TaskUnassignedEvent(previousUserId, task.price)) >> sendEvent(task.taskId, TaskAssignedEvent(task.assignedUser.userId, task.price)).void
        else
          ().pure[F]
      }
    } yield result

  private def sendEvent(taskId: TaskId, event: TaskBusinessEvent): F[F[ProducerResult[TaskId, TaskBusinessEvent]]] =
    producer.produceOne(ProducerRecord(config.topic, taskId, event))
}

object TaskServiceKafkaImplementation {

  implicit private val configuration: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit private val userIdEncoder: Encoder[UserId] =
    deriveUnwrappedEncoder

  implicit private val taskIdEncoder: Encoder[TaskId] =
    deriveUnwrappedEncoder

  sealed trait TaskBusinessEvent

  case class TaskAssignedEvent(assignedUserId: UserId, price: BigDecimal) extends TaskBusinessEvent
  case class TaskCompletedEvent(assignedUserId: UserId, price: BigDecimal) extends TaskBusinessEvent
  case class TaskUnassignedEvent(unassignedUserId: UserId, price: BigDecimal) extends TaskBusinessEvent

  object TaskBusinessEvent {

    implicit val encoder: Encoder[TaskBusinessEvent] =
      deriveConfiguredEncoder
  }

  implicit def taskIdSerializer[F[_]: Sync]: KafkaSerializer[F, TaskId] =
    KafkaSerializer.string.contramap(_.value.asJson.noSpaces)

  implicit def userStreamingEventSerializer[F[_]: Sync]: KafkaSerializer[F, TaskBusinessEvent] =
    KafkaSerializer.string.contramap(_.asJson.noSpaces)

  def make[F[_]: Async](underlying: TaskService[F], config: TaskBusinessProducerConfig): Resource[F, TaskService[F]] =
    KafkaProducer
      .resource(ProducerSettings[F, TaskId, TaskBusinessEvent].withBootstrapServers(config.bootstrapServer))
      .map(new TaskServiceKafkaImplementation[F](underlying, config, _))
}
