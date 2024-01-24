package com.github.a7emenov.billing_service.consumers

import cats.effect.Async
import cats.effect.kernel.Sync
import com.github.a7emenov.billing_service.configuration.TaskBusinessConsumerConfig
import com.github.a7emenov.billing_service.domain.UserId
import com.github.a7emenov.billing_service.services.BalanceService
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, KafkaConsumer, Deserializer => KafkaDeserializer}
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.parser.decode

object TaskBusinessConsumer {

  implicit private val configuration: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit private val userIdDecoder: Decoder[UserId] =
    deriveUnwrappedDecoder

  sealed trait TaskBusinessEvent

  case class TaskAssignedEvent(assignedUserId: UserId, price: BigDecimal) extends TaskBusinessEvent
  case class TaskCompletedEvent(assignedUserId: UserId, price: BigDecimal) extends TaskBusinessEvent
  case class TaskUnassignedEvent(unassignedUserId: UserId, price: BigDecimal) extends TaskBusinessEvent

  object TaskBusinessEvent {

    implicit val decoder: Decoder[TaskBusinessEvent] =
      deriveConfiguredDecoder
  }

  implicit def userStreamingEventDeserializer[F[_]: Sync]: KafkaDeserializer[F, TaskBusinessEvent] =
    KafkaDeserializer.lift(bytes => Sync[F].fromEither(decode[TaskBusinessEvent](new String(bytes))))

  def stream[F[_]: Async](balanceService: BalanceService[F], config: TaskBusinessConsumerConfig): fs2.Stream[F, Unit] =
    KafkaConsumer
      .stream(ConsumerSettings[F, String, TaskBusinessEvent]
        .withBootstrapServers(config.bootstrapServer)
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withGroupId("billing-service.task-business-group")
      )
      .subscribeTo(config.topic)
      .flatMap(_.stream)
      .evalMap { record =>
        record.record.value match {
          case TaskAssignedEvent(assignedUserId, price) =>
            balanceService.update(assignedUserId, -price)
          case TaskCompletedEvent(assignedUserId, price) =>
            balanceService.update(assignedUserId, 2*price)
          case TaskUnassignedEvent(unassignedUserId, price) =>
            balanceService.update(unassignedUserId, price)
        }
      }
}
