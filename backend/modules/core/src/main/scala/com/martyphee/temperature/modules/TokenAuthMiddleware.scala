package com.martyphee.temperature.modules

import cats.MonadError
import cats.data.{Kleisli, OptionT}
import cats.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
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

    def validateToken(authorization: String): Option[String] =
      authorization.replace("Bearer", "").trim match {
        case token if token.isEmpty => None
        case token => Some(token)
      }

    val authUser: Kleisli[F, Request[F], Either[String, A]] =
      Kleisli { request =>
        val message = for {
          header <- request.headers.get(Authorization).toRight("Couldn't find an Authorization header")
          token <- validateToken(header.value).toRight("Invalid token")
          message <- Either.catchOnly[NumberFormatException](token).leftMap(_.toString)
        } yield message

        message.traverse(retrieveUser(_).flatMap(_.get.pure[F]))


        //       message.traverse(token => retrieveUser(token))
        //       message.fold(_ => "not found".asLeft[A].pure[F], _.asRight[String].pure[F])
        //       message.flatMap(retrieveUser(_).map(_.fold("not found".asLeft[A])(_.asRight[String])))
        //       message.traverse(retrieveUser(_).flatMap { value =>
        //         if(value.isDefined) {
        //           value.get.pure[F]
        //         } else {
        //          value.getOrElse("Empty").asInstanceOf[A].pure[F]
        //         }
        //       })
      }

    // Kleisli[F, Request[F], Either[Err, T]]
    AuthMiddleware(authUser, onFailure)
  }
}
