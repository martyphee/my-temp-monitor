package com.martyphee.temperature.algebras

import cats.effect._
import cats.syntax.all._
import com.martyphee.temperature.domain.TemperatureEvent.{ EventType, _ }
import com.martyphee.temperature.effects._
import skunk._
import skunk.codec.all._
import skunk.implicits._

trait Events[F[_]] {
  def create(event: CreateTemperatureEvent): F[Unit]
  def findAll: F[List[TemperatureEvent]]
}

object LiveEvents {
  def make[F[_]: Sync](
    sessionPool: Resource[F, Session[F]]
  ): F[Events[F]] =
    Sync[F].delay(
      new LiveEvents[F](sessionPool)
    )
}

final class LiveEvents[F[_]: Sync: GenUUID] private (sessionPool: Resource[F, Session[F]]) extends Events[F] {
  import EventsQueries._

  override def create(event: CreateTemperatureEvent): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertEvent).use { cmd =>
        GenUUID[F].make[EventId].flatMap { id =>
          cmd
            .execute(id ~ event.eventType)
            .void
        }
      }
    }

  override def findAll: F[List[TemperatureEvent]] =
    sessionPool.use(session => session.prepare(selectAll).use(ps => ps.stream(Void, 1024).compile.toList))

  private object EventsQueries {
    val insertEvent: Command[EventId ~ EventType] =
      sql"""
         insert into event
         values ($uuid, $varchar)""".command.contramap {
        case id ~ t =>
          id.value ~ t.toString.toLowerCase()
      }

    val queryDecoder: Decoder[TemperatureEvent] =
      (uuid ~ varchar ~ timestamp).map {
        case u ~ v ~ t =>
          TemperatureEvent(
            EventId(u),
            EventType.withName(v),
            EventCreatedAt(t)
          )
      }
  }
  val selectAll: Query[Void, TemperatureEvent] =
    sql"""
      select * from event order by created_at desc limit 20
    """.query(queryDecoder)
}
