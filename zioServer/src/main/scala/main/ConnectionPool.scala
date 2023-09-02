package main

import javax.sql.DataSource
import zio.ZIO
import zio.ZLayer
import cats.*
import cats.data.*
import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.util.ExecutionContexts
import org.postgresql.ds.PGSimpleDataSource

object ConnectionPool {


//  val xa = Transactor.fromDriverManager[IO](
//    driver = "org.postgresql.Driver", // JDBC driver classname
//    url = "jdbc:postgresql:world", // Connect URL - Driver specific
//    user = "postgres", // Database user name
//    password = "password", // Database password
//    logHandler = None // Don't setup logging for now. See Logging page for how to log events in detail
//  )

  type Conf = Any

  val layer: ZLayer[Conf, Throwable, DataSource] = ZLayer.fromZIO(
    ZIO.serviceWithZIO[Conf] { conf =>
      ZIO.attemptBlocking {
        val ds = new PGSimpleDataSource
        ds.setServerNames(Array("localhost"))
        ds.setPortNumbers(Array(5432))
        ds.setDatabaseName("testerDB")
        ds.setUser("postgres")
        ds.setPassword("password")
        ds.setCurrentSchema("tester")
        ds.setApplicationName("tester")
        ds
      }
    }
  )

}
