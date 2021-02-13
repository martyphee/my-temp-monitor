package com.martyphee.temperature.domain

import enumeratum._
import enumeratum.EnumEntry._
import io.estatico.newtype.macros.newtype

import java.time.LocalDateTime
import java.util.UUID

object TemperatureEvent {
  sealed abstract class EventType extends EnumEntry with Lowercase

  object EventType extends Enum[EventType] {
    case object FanOn  extends EventType
    case object FanOff extends EventType

    val values = findValues
  }
  @newtype case class EventId(value: UUID)
  @newtype case class EventCreatedAt(createdAt: LocalDateTime)

  case class TemperatureEvent(id: EventId, eventType: EventType, createdAt: EventCreatedAt)

  case class CreateTemperatureEvent(eventType: EventType)
}
