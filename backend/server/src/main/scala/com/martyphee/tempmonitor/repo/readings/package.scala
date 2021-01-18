package com.martyphee.tempmonitor.repo

import com.martyphee.tempmonitor.ExpectedFailure
import com.martyphee.tempmonitor.db.SessionPool
import com.martyphee.tempmonitor.model.Reading
import zio.{Has, ZIO, ZLayer}

package object readings {
  type ReadingsRepo = Has[ReadingsRepo.Service]

  object ReadingsRepo {

    trait Service {
      def get(id: Long): ZIO[Any, ExpectedFailure, Option[Reading]]

      def create(user: Reading): ZIO[Any, ExpectedFailure, Unit]

      def delete(id: Long): ZIO[Any, ExpectedFailure, Unit]
    }

    val live: ZLayer[SessionPool, Nothing, ReadingsRepo] = ZLayer.fromService { db: SessionPool.Service =>
      new Service {
        override def get(id: Long): ZIO[Any, ExpectedFailure, Option[Reading]] = ???

        override def create(user: Reading): ZIO[Any, ExpectedFailure, Unit] = ???

        override def delete(id: Long): ZIO[Any, ExpectedFailure, Unit] = ???
      }
    }
  }

  def get(id: Long): ZIO[ReadingsRepo, ExpectedFailure, Option[Reading]] =
    ZIO.accessM(_.get.get(id))

  def create(user: Reading): ZIO[ReadingsRepo, ExpectedFailure, Unit] =
    ZIO.accessM(_.get.create(user))

  def delete(id: Long): ZIO[ReadingsRepo, ExpectedFailure, Unit] =
    ZIO.accessM(_.get.delete(id))
}
