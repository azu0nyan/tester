package EmbeddedPG

import doobie.util.transactor.Transactor
import io.github.gaelrenoux.tranzactio.DatabaseOps
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import zio.*

import java.sql.DatabaseMetaData
import javax.sql.DataSource
import scala.io.Source
import io.github.gaelrenoux.tranzactio.doobie.*

object EmbeddedPG {
  val schemaSql: String = Source.fromResource("schema.sql").getLines().mkString("\n")

  //LC_ALL=en_US.UTF-8;LC_CTYPE=en_US.UTF-8
  val dataSource: Task[DataSource] =
    ZIO.succeed {
      val epg = EmbeddedPostgres.builder()
        .start()
      val conn = epg.getPostgresDatabase.getConnection
      conn.prepareStatement(schemaSql).execute()
      conn.close()
      epg.getPostgresDatabase
    }

  val dataSourceLayer: ZLayer[Any, Throwable, DataSource] = ZLayer.fromZIO(dataSource)

  val databaseLayer: ZLayer[Any, Throwable, Database] = dataSourceLayer >>> Database.fromDatasource

  //  val connectionLayer: ZLayer[Database, Throwable, Connection] =
  //    ZIO.serviceWith[Database]{ db => db.transactionOrDie()
  //
  //    }
  val connection: Task[Connection] = for {
    ds <- dataSource
    conn <- Database.connectionFromJdbc(ds.getConnection)
  } yield conn

  val connectionLayer = ZLayer.fromZIO(connection)

  //    val transactor = Transactor.fromDriverManager(
  //      "org.postgresql.Driver",
  //      postgres.getJdbcUrl("postgres", "postgres"),
  //      "postgres",
  //      "postgres"
  //    )
  //    transactor
}
//