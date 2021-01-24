package com.martyphee.temperature.modules

import cats._
import cats.effect._
import cats.implicits._
import com.martyphee.temperature.http.auth.users._
import com.martyphee.temperature.http.routes._
import com.martyphee.temperature.http.secured.ReadingRoutes
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware._

import scala.concurrent.duration._

object HttpApi {
  def make[F[_]: Concurrent: Timer](
    algebras: Algebras[F],
    loginToken: String
  ): F[HttpApi[F]] =
    Sync[F].delay(
      new HttpApi[F](
        algebras,
        loginToken
      )
    )
}

trait UsersAuth[F[_], A] {
  def findUser(token: String): F[Option[A]]
}

class LiveUserAuth[F[_]: Applicative](loginToken: String) extends UsersAuth[F, CommonUser] {

  def findUser(token: String): F[Option[CommonUser]] = //Some(CommonUser(User(AuthToken(token)))).pure[F]
    (token == loginToken)
      .guard[Option]
      .as(CommonUser(User(AuthToken(token))))
      .pure[F]
}

final class HttpApi[F[_]: Concurrent: Timer] private (algebras: Algebras[F], loginToken: String) {

  private val healthRoutes = new HealthRoutes[F](algebras.healthCheck).routes

  private val userAuth: UsersAuth[F, CommonUser] = new LiveUserAuth(loginToken)

  private val usersMiddleware = TokenAuthMiddleware[F, CommonUser](userAuth.findUser)

  // Secured Routes
  private val readingRoutes = new ReadingRoutes[F](algebras.readings)

  // Combining all the http routes
  private val openRoutes: HttpRoutes[F] = healthRoutes <+> readingRoutes.routes

  private val routes: HttpRoutes[F] = Router(
    version.v1 -> openRoutes,
    version.v1 -> readingRoutes.secureRoutes(usersMiddleware)
  )

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[F] =>
      CORS(http, CORS.DefaultCORSConfig)
    } andThen { http: HttpRoutes[F] => Timeout(60.seconds)(http) }
  }

  private val loggers: HttpApp[F] => HttpApp[F] = {
    { http: HttpApp[F] => RequestLogger.httpApp(logHeaders = true, logBody = true)(http) } andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(logHeaders = true, logBody = true)(http)
    }
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)
}
