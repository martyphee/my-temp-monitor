package com.martyphee.temperature.config

import cats.effect._
import cats.syntax.all._
import ciris._
import ciris.refined._
import com.martyphee.temperature.config.data._
import com.martyphee.temperature.config.environments.AppEnvironment._
import com.martyphee.temperature.config.environments._
import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string._

import java.net.URI
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
      env("SC_LOGIN_TOKEN").as[NonEmptyString].secret,
      env("DATABASE_URL").as[NonEmptyString].secret,
      env("PORT").as[UserPortNumber]
    ).parMapN { (token, dbUrl, httpPort) =>
      // This feels a bit ugly. Need to rethink it?
      val uri = URI.create(dbUrl.value)
      val host: Either[String, NonEmptyString] = refineV(uri.getHost)
      val port: Either[String, UserPortNumber] = refineV(uri.getPort)
      val user: Either[String, NonEmptyString] = refineV(uri.getUserInfo.split(":")(0))
      val password: Either[String, NonEmptyString] = refineV(uri.getUserInfo.split(":")(1))
      val database: Either[String, NonEmptyString] = refineV(uri.getPath.substring(1))

      AppConfig(
        HttpClientConfig(
          connectTimeout = 2.seconds,
          requestTimeout = 2.seconds
        ),
        PostgreSQLConfig(
          host = host.getOrElse("localhost"),
          port = port.getOrElse(5432),
          user = user.getOrElse("postgres"),
          password = password.getOrElse("postgres"),
          database = database.getOrElse("temperature"),
          max = 10
        ),
        RedisConfig(RedisURI("redis://127.0.0.1")),
        HttpServerConfig(
          host = "0.0.0.0",
          port = httpPort
        ),
        token.value
      )
    }
}
