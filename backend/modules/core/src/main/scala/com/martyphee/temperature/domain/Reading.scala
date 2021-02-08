package com.martyphee.temperature.domain

import io.estatico.newtype.macros.newtype
import java.time.LocalDateTime
import java.util.UUID

object Reading {

  @newtype case class ReadingId(value: UUID)

  object ReadingTemperature {
    def apply(temp: String): ReadingTemperature = ReadingTemperature(BigDecimal(temp))
  }

  @newtype case class ReadingTemperature(value: BigDecimal)
  @newtype case class ReadingCreatedAt(createdAt: LocalDateTime)

  case class Reading(id: ReadingId, temperature: ReadingTemperature, createdAt: ReadingCreatedAt)

  case class ReadingParam(value: ReadingTemperature)
}
