package com.github.a7emenov.task_service.domain

sealed abstract class TaskStatus(val name: String)

object TaskStatus {

  case object InProgress extends TaskStatus("in_progress")
  case object Completed extends TaskStatus("completed")
}
