package com.martyphee.tempmonitor

import scala.jdk.CollectionConverters._
import cats.effect.ExitCode
import com.martyphee.tempmonitor.db._
import com.typesafe.scalalogging.LazyLogging
import natchez.Trace.Implicits.noop
import com.martyphee.tempmonitor.config.{Application, DBConfig, DBConfigService}
import com.martyphee.tempmonitor.http.API
import com.martyphee.tempmonitor.repo.readings.ReadingsRepo
import com.martyphee.tempmonitor.module.logger.{LiveLogger, Logger => MyLogger}
import tapir.swagger.http4s.SwaggerHttp4s
import zio.{ZEnv, ExitCode => ZExitCode, _}
import zio.console._
import tapir.docs.openapi._
import tapir.openapi.circe.yaml._
import eu.timepit.refined.auto._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import com.typesafe.config.{Config, ConfigFactory}
import zio._
import zio.clock.Clock
import zio.console.putStrLn
import zio.interop.catz._

import scala.concurrent.ExecutionContext
import scala.util.Try

object TempMonitor extends App with LazyLogging {
//  type AppEnvironment = Console with SessionPool with ReadingsRepo with MyLogger
//
//  val appEnvironment: ZLayer[Any, Throwable, Console with DBConfigService with SessionPool] = Console.live >+> DBConfig.live >+> SessionPool.live
//
//  private val userRoute = new API[AppEnvironment]
//  private val yaml = userRoute.getEndPoints.toOpenAPI("User", "1.0").toYaml
//  private val httpApp =
//    Router("/" -> userRoute.getRoutes, "/docs" -> new SwaggerHttp4s(yaml).routes[RIO[AppEnvironment, *]]).orNotFound
//  private val finalHttpApp = Logger.httpApp[ZIO[AppEnvironment, Throwable, *]](logHeaders = true, logBody = true)(httpApp)

  // Domain classes, services, layers
  case class Pet(species: String, url: String)

  object PetLayer {
    type PetService = Has[PetService.Service]

    object PetService {
      trait Service {
        def find(id: Int): ZIO[Any, String, Pet]
      }

      val live: ZLayer[Console, String, PetService] = ZLayer.fromFunction { console: Console => petId: Int =>
        console.get.putStrLn(s"Got request for pet: $petId") *> {
          if (petId == 35) {
            UIO(Pet("Tapirus terrestris", "https://en.wikipedia.org/wiki/Tapir"))
          } else {
            IO.fail("Unknown pet id")
          }
        }
      }

      def find(id: Int): ZIO[PetService, String, Pet] = ZIO.accessM(_.get.find(id))
    }
  }
  import PetLayer.PetService

  // Documentation
  val yaml: String = {
    import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
    import sttp.tapir.openapi.circe.yaml._
    OpenAPIDocsInterpreter.toOpenAPI(List(petEndpoint), "Our pets", "1.0").toYaml
  }

  // Starting the server
  val serve: ZIO[ZEnv with PetService, Throwable, Unit] = ZIO.runtime[ZEnv with PetService].flatMap { implicit runtime =>
    BlazeServerBuilder[RIO[PetService with Clock, *]](runtime.platform.executor.asEC)
      .bindHttp(8080, "localhost")
      .withHttpApp(Router("/" -> (petRoutes <+> new SwaggerHttp4s(yaml).routes)).orNotFound)
      .serve
      .compile
      .drain
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    serve.provideCustomLayer(PetService.live).exitCode
  }
}
