package com.martyphee.temperature.domain

import io.estatico.newtype.macros._

object healthcheck {
  @newtype case class RedisStatus(value: Boolean)
  @newtype case class PostgresStatus(value: Boolean)

  case class AppStatus(
    postgres: PostgresStatus
  )
}
