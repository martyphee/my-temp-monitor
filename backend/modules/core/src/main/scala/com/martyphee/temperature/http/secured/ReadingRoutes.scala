package com.martyphee.temperature.http.secured

import cats._
import cats.syntax.all._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import com.martyphee.temperature.algebras.Readings
import com.martyphee.temperature.domain.Reading._
import com.martyphee.temperature.http.auth.users._
import com.martyphee.temperature.http.json._

final class ReadingRoutes[F[_]: Defer: JsonDecoder: Monad](
  readings: Readings[F]
) extends Http4sDsl[F] {

  private[secured] val prefixPath = "/reading"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {
    // Add items to the cart
    case ar @ POST -> Root as _ =>
      ar.req.asJsonDecode[ReadingParam].flatMap(reading => Created(readings.create(reading)))
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
