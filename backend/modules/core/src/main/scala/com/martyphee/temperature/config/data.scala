package com.martyphee.temperature.config

import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

import scala.concurrent.duration.FiniteDuration

object data {

  case class AppConfig(
    httpClientConfig: HttpClientConfig,
    postgreSQL: PostgreSQLConfig,
    redis: RedisConfig,
    httpServerConfig: HttpServerConfig,
    loginToken: String
  )

  case class PostgreSQLConfig(
    host: NonEmptyString,
    port: UserPortNumber,
    user: NonEmptyString,
    database: NonEmptyString,
    max: PosInt
  )

  @newtype case class RedisURI(value: NonEmptyString)
  @newtype case class RedisConfig(uri: RedisURI)

  case class HttpServerConfig(
    host: NonEmptyString,
    port: UserPortNumber
  )

  case class HttpClientConfig(
    connectTimeout: FiniteDuration,
    requestTimeout: FiniteDuration
  )

}
