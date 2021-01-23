package com.martyphee.temperature

import cats.effect._
import cats.syntax.all._
import com.martyphee.temperature.config.data._
import io.chrisdavenport.log4cats.Logger
import natchez.Trace.Implicits.noop
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import skunk._

import scala.concurrent.ExecutionContext

final case class AppResources[F[_]](
  client: Client[F],
  psql: Resource[F, Session[F]],
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

    def mkHttpClient(c: HttpClientConfig): Resource[F, Client[F]] =
      BlazeClientBuilder[F](ExecutionContext.global)
        .withConnectTimeout(c.connectTimeout)
        .withRequestTimeout(c.requestTimeout)
        .resource

    (
      mkHttpClient(cfg.httpClientConfig),
      mkPostgreSqlResource(cfg.postgreSQL),
    ).mapN(AppResources.apply[F])
  }
}
