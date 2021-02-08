package com.temperature

import cats.effect._
import cats.implicits.{catsSyntaxEq => _, _}
import com.martyphee.temperature.algebras.{LiveEvents, LiveReadings}
import com.martyphee.temperature.domain.Reading.ReadingParam
import com.martyphee.temperature.domain.TemperatureEvent.CreateTemperatureEvent
import natchez.Trace.Implicits.noop
import skunk._
import suite.{IOAssertion, ResourceSuite}
import temperature.arbitraries._

class PostgresTest extends ResourceSuite[Resource[IO, Session[IO]]] {
  // For it:tests, one test is enough
  val MaxTests: PropertyCheckConfigParam = MinSuccessful(1)

  override def resources =
    Session.pooled[IO](
      host = "localhost",
      port = 5432,
      user = "postgres",
      database = "temperature_test",
      max = 10
    )

  withResources { pool =>
    test("Readings") {
      forAll(MaxTests) { (reading: ReadingParam) =>
        IOAssertion {
          for {
            c <- LiveReadings.make[IO](pool)
            x <- c.findAll
            _ <- c.create(reading)
            y <- c.findAll
//            _ <- IO.pure(println(s"saved: ${y.head.temperature.value}, gen: ${reading.value.value}"))
          } yield assert(
            x.isEmpty && y.count(_.temperature.value === reading.value.value) === 1
          )
        }
      }
    }
  }

  withResources { pool =>
    test("Events") {
      forAll(MaxTests) { (event: CreateTemperatureEvent) =>
        IOAssertion {
          for {
            c <- LiveEvents.make[IO](pool)
            x <- c.findAll
            _ <- c.create(event)
            y <- c.findAll
          } yield assert(
            x.isEmpty && y.count(_.eventType.toString === event.eventType.toString) === 1
          )
        }
      }
    }
  }
}
