package com.martyphee.temperature.modules

import cats.MonadError
import cats.data.{Kleisli, OptionT}
import cats.implicits._
import dev.profunktor.auth.AuthHeaders
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRoutes, Request}

object TokenAuthMiddleware {
  def apply[F[_] : MonadError[*[_], Throwable], A](
                                                    retrieveUser: String => F[Option[A]]
                                                  ): AuthMiddleware[F, A] = {
    val dsl = new Http4sDsl[F] {};
    import dsl._

    val onFailure: AuthedRoutes[String, F] =
      Kleisli(req => OptionT.liftF(Forbidden(req.context)))

    val authUser: Kleisli[F, Request[F], Either[String, A]] =
      Kleisli { request =>
        AuthHeaders.getBearerToken(request).fold("Bearer token not found".asLeft[A].pure[F]) { token =>
          retrieveUser(token.value)
            .map(_.fold("not found".asLeft[A])(_.asRight[String]))
            .recover {
              case _: Exception => "Invalid access token".asLeft[A]
            }
        }
      }

    // Kleisli[F, Request[F], Either[Err, T]]
    AuthMiddleware(authUser, onFailure)
  }
}
