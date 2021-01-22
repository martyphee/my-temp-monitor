package com.martyphee.temperature.config

import cats.effect._
import cats.syntax.all._
import ciris._
import ciris.refined._
import com.martyphee.temperature.config.data._
import com.martyphee.temperature.config.environments.AppEnvironment._
import com.martyphee.temperature.config.environments._
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import eu.timepit.refined.types.string.NonEmptyString
import scala.concurrent.duration._

object load {
  // Ciris promotes configuration as code
  def apply[F[_]: Async: ContextShift]: F[AppConfig] =
    env("SC_APP_ENV")
      .as[AppEnvironment]
      .flatMap {
        case Test =>
          default()
        case Prod =>
          default()
      }
      .load[F]

  private def default(): ConfigValue[AppConfig] =
    (
      env("SC_JWT_SECRET_KEY").as[NonEmptyString].secret,
      env("SC_JWT_CLAIM").as[NonEmptyString].secret,
      env("SC_LOGIN_TOKEN").as[NonEmptyString].secret
    ).parMapN { (_, _, token) =>
      AppConfig(
        HttpClientConfig(
          connectTimeout = 2.seconds,
          requestTimeout = 2.seconds
        ),
        PostgreSQLConfig(
          host = "localhost",
          port = 5432,
          user = "postgres",
          database = "temperature",
          max = 10
        ),
        RedisConfig(RedisURI("redis://127.0.0.1")),
        HttpServerConfig(
          host = "0.0.0.0",
          port = 8080
        ),
        token.value
      )
    }
}
