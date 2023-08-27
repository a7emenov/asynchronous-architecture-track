package com.github.a7emenov.task_service.domain

case class User(
                 userId: UserId,
                 firstName: String,
                 lastName: String,
                 role: UserRole)
