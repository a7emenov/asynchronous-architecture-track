package com.github.a7emenov.billing_service.domain

sealed abstract class UserRole(val name: String)

object UserRole {

  case object Admin extends UserRole("admin")
  case object Manager extends UserRole("manager")
  case object RegularEmployee extends UserRole("regular_employee")
}
