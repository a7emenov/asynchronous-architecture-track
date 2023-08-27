package com.github.a7emenov.task_service.domain

case class Task(
  taskId: TaskId,
  description: String,
  price: BigDecimal,
  assignedUser: User,
  status: TaskStatus)
