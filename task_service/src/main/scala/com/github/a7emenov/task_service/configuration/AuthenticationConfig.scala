package com.github.a7emenov.task_service.configuration

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class AuthenticationConfig(
  authenticationServiceHost: String,
  authenticationServicePort: Int)

object AuthenticationConfig {

  implicit val reader: ConfigReader[AuthenticationConfig] =
    deriveReader
}
