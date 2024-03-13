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

  import pureconfig._
  import pureconfig.generic.derivation.default._

  final case class DatabaseConfig(
                                   className: String,
                                   serverName: String,
                                   portNumber: String,
                                   databaseName: String,
                                   currentSchema: String,
                                   applicationName: String,

                                   user: String,
                                   password: String,
                                   connectionTimeout: String,
                                 )derives ConfigReader


  val config = ZLayer.succeed {
    val config: ConfigReader.Result[DatabaseConfig] = ConfigSource.resources("application.conf").at("database").load[DatabaseConfig]
    if (config.isLeft) println(config) //todo log better
    config.right.get
  }
  //  val xa = Transactor.fromDriverManager[IO](
  //    driver = "org.postgresql.Driver", // JDBC driver classname
  //    url = "jdbc:postgresql:world", // Connect URL - Driver specific
  //    user = "postgres", // Database user name
  //    password = "password", // Database password
  //    logHandler = None // Don't setup logging for now. See Logging page for how to log events in detail
  //  )

  type Conf = DatabaseConfig

  lazy val layer: ZLayer[Any, Throwable, DataSource] = ZLayer.fromZIO(
    ZIO.serviceWithZIO[Conf] { conf =>
      ZIO.attemptBlocking {
        val ds = new PGSimpleDataSource
        ds.setServerNames(Array(conf.serverName))
        ds.setPortNumbers(Array(conf.portNumber.toInt))
        ds.setDatabaseName(conf.databaseName)
        ds.setUser(conf.user)
        ds.setPassword(conf.password)
        ds.setCurrentSchema(conf.currentSchema)
        ds.setApplicationName(conf.applicationName)
        ds
      }
    }.provideSomeLayer(config)
  )

}
