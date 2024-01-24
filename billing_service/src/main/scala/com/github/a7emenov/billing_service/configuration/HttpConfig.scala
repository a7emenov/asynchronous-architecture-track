package com.github.a7emenov.billing_service.configuration

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class HttpConfig(
  host: String,
  port: Int)

object HttpConfig {

  implicit val reader: ConfigReader[HttpConfig] =
    deriveReader
}
