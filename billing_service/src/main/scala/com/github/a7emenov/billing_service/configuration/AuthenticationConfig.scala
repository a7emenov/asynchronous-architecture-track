package com.github.a7emenov.billing_service.configuration

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class AuthenticationConfig(secretKey: String)

object AuthenticationConfig {

  implicit val reader: ConfigReader[AuthenticationConfig] =
    deriveReader
}
