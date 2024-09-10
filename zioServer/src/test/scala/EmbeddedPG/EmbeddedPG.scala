package EmbeddedPG

import cats.effect.Resource
import doobie.free.KleisliInterpreter
import doobie.util.transactor.{Strategy, Transactor}
import io.github.gaelrenoux.tranzactio.DatabaseOps
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import zio.*

import java.sql.{DatabaseMetaData, Connection as JdbcConnection}
import javax.sql.DataSource
import scala.io.Source
import io.github.gaelrenoux.tranzactio.doobie.*
import zio.interop.catz.*

object EmbeddedPG {
  val schemaSqlLines = Source.fromResource("schema.sql").getLines()
  val sqlStatements: Seq[String] =  schemaSqlLines
    .filter(str => !str.isBlank)
    .filter(str => !str.startsWith("--"))
    .mkString("\n")
    .split(";")
    .map(_ + ";")

  def initDb(db: DataSource) : Unit = {
    val conn = db.getConnection
    println(s"Creating test schema")
    val stmt = conn.createStatement()
    for(sql <- sqlStatements) {
      println(s"Executing ${sql}")
      val initResult = stmt.execute(sql)
      println(s"Exec result $initResult")
    }
    stmt.close()
    println(s"Db init finished")
    conn.close()

  }

  //LC_ALL=en_US.UTF-8;LC_CTYPE=en_US.UTF-8
  val dataSource: Task[DataSource] =
    ZIO.succeed {
      val epg = EmbeddedPostgres.builder()
        .start()
      initDb(epg.getPostgresDatabase)

      epg.getPostgresDatabase
    }

  val dataSourceLayer: ZLayer[Any, Throwable, DataSource] = ZLayer.fromZIO(dataSource)

  val databaseLayer: ZLayer[Any, Throwable, Database] = dataSourceLayer >>> Database.fromDatasource

  val connection: Task[Connection] = for {
    ds <- dataSource
    conn <- ZIO.succeed {
      val res = ds.getConnection
      res.setSchema("tester")
      val connect = (c: JdbcConnection) => Resource.pure[Task, JdbcConnection](c)
      val interp = KleisliInterpreter[Task](doobie.util.log.LogHandler.noop).ConnectionInterpreter
      val tran = Transactor(res, connect, interp, Strategy.void)
      tran
    }
  } yield conn

  val connectionLayer: ZLayer[Any, Throwable, Connection] = ZLayer.fromZIO(connection)

//      val transactor = Transactor.fromDriverManager(
  //      "org.postgresql.Driver",
  //      postgres.getJdbcUrl("postgres", "postgres"),
  //      "postgres",
  //      "postgres"
  //    )
  //    transactor
}
//