package com.martyphee.temperature

import cats.effect._
import com.martyphee.temperature.config.data._
import io.chrisdavenport.log4cats.Logger
import natchez.Trace.Implicits.noop
import skunk._

final case class AppResources[F[_]](
  psql: Resource[F, Session[F]]
)

object AppResources {

  def make[F[_]: ConcurrentEffect: ContextShift: Logger](
    cfg: AppConfig
  ): Resource[F, AppResources[F]] = {

    def mkPostgreSqlResource(c: PostgreSQLConfig): SessionPool[F] =
      Session
        .pooled[F](
          host = c.host.value,
          port = c.port.value,
          user = c.user.value,
          password = Some(c.password.value),
          database = c.database.value,
          max = c.max.value
        )

    (
      mkPostgreSqlResource(cfg.postgreSQL)
    ).map(AppResources.apply[F])
  }
}
