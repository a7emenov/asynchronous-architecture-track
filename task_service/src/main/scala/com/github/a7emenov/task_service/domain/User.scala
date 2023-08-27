package com.github.a7emenov.task_service.domain

case class User(
               id: UserId,
  firstName: String,
  lastName: String,
  role: UserRole
               )
