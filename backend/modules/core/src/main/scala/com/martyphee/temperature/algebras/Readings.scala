package com.martyphee.temperature.algebras

import cats.effect._
import cats.syntax.all._
import com.martyphee.temperature.domain.Reading._
import com.martyphee.temperature.effects.{BracketThrow, GenUUID}
import skunk._
import skunk.codec.all._
import skunk.implicits._

import java.time.LocalDateTime

trait Readings[F[_]] {
  def create(temperature: ReadingParam): F[Unit]
  def findAll: F[List[Reading]]
}

object LiveReadings {
  def make[F[_]: Sync](
    sessionPool: Resource[F, Session[F]]
  ): F[Readings[F]] =
    Sync[F].delay(
      new LiveReadings[F](sessionPool)
    )
}

final class LiveReadings[F[_]: Sync: BracketThrow: GenUUID] private (sessionPool: Resource[F, Session[F]])
    extends Readings[F] {
  import ReadingsQueries._

  def create(temperature: ReadingParam): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertBrand).use { cmd =>
        GenUUID[F].make[ReadingId].flatMap { id =>
          cmd
            .execute(Reading(id, ReadingTemperature(temperature.value.value), ReadingCreatedAt(LocalDateTime.now())))
            .void
        }
      }
    }

  override def findAll: F[List[Reading]] =
    sessionPool.use { session =>
      session.prepare(selectAll).use { ps =>
        ps.stream(Void, 1024).compile.toList
      }
    }
}

private object ReadingsQueries {
  val codec: Codec[Reading] =
    (uuid ~ numeric(5,3) ~ timestamp).imap {
      case i ~ n ~ t => Reading(ReadingId(i), ReadingTemperature(n), ReadingCreatedAt(t))
    }(b => b.id.value ~ b.temperature.value ~ b.createdAt.createdAt)

  val selectAll: Query[Void, Reading] =
    sql"""
         select id, temperature, created_at
         from reading
         order by created_at
         limit 20
         """.query(codec)

  val insertBrand: Command[Reading] =
    sql"""
        INSERT INTO reading
        VALUES ($codec)
        """.command
}
