package com.martyphee.temperature.algebras

import cats.Parallel
import cats.effect._
import cats.effect.implicits._
import cats.syntax.all._
import com.martyphee.temperature.domain.healthcheck._

import scala.concurrent.duration._
import skunk._
import skunk.codec.all._
import skunk.implicits._

trait HealthCheck[F[_]] {
  def status: F[AppStatus]
}

object LiveHealthCheck {
  def make[F[_]: Concurrent: Parallel: Timer](
    sessionPool: Resource[F, Session[F]]
  ): F[HealthCheck[F]] =
    Sync[F].delay(
      new LiveHealthCheck[F](sessionPool)
    )
}

final class LiveHealthCheck[F[_]: Concurrent: Parallel: Timer] private (
  sessionPool: Resource[F, Session[F]]
) extends HealthCheck[F] {

  val q: Query[Void, Int] =
    sql"SELECT 1".query(int4)

  val postgresHealth: F[PostgresStatus] =
    sessionPool
      .use(_.execute(q))
      .map(_.nonEmpty)
      .timeout(1.second)
      .orElse(false.pure[F])
      .map(PostgresStatus.apply)

  val status: F[AppStatus] =
    postgresHealth.map(AppStatus)

}
