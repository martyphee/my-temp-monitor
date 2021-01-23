package com.martyphee.temperature.modules

import cats.Parallel
import cats.effect._
import cats.syntax.all._
import com.martyphee.temperature.algebras._
import skunk._

object Algebras {
  def make[F[_]: Concurrent: Parallel: Timer](
    sessionPool: Resource[F, Session[F]]
  ): F[Algebras[F]] =
    for {
      health   <- LiveHealthCheck.make[F](sessionPool)
      readings <- LiveReadings.make[F](sessionPool)
    } yield new Algebras[F](health, readings)
}

final class Algebras[F[_]] private (
  val healthCheck: HealthCheck[F],
  val readings: Readings[F]
)
