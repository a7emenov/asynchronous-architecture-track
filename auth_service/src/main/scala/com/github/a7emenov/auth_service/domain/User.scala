package com.github.a7emenov.auth_service.domain

case class User(
  firstName: String,
  lastName: String,
  role: UserRole)
