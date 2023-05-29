package dbCodeGen

import io.getquill.codegen.model.{LiteralNames, SnakeCaseNames}

import scala.reflect.classTag

object Main {
  def main(args: Array[String]): Unit = {
    /*
    // provide DB credentials with a com.typesafe.config.Config object
    // (under the hood the credentials are used to create a HikariPool DataSource)
    import io.getquill.codegen.jdbc.SimpleJdbcCodegen
    import io.getquill.util.LoadConfig

    val snakecaseConfig = LoadConfig(configPrefix: String)
    val gen = new SimpleJdbcCodegen(snakecaseConfig, "com.my.project") {
      override def nameParser = SnakeCaseNames
    }
    gen.writeFiles("src/main/scala/com/my/project")
*/
    // or, provide an initialized DataSource
    import io.getquill.codegen.jdbc.SimpleJdbcCodegen
    import org.postgresql.ds.PGSimpleDataSource

    val pgDataSource = new PGSimpleDataSource()
    pgDataSource.setURL(
      "jdbc:postgresql://127.0.0.1:5432/testertest2?ssl=false",
    )
    pgDataSource.setUser("postgres")
    pgDataSource.setPassword("password")
    val gen = new SimpleJdbcCodegen(pgDataSource, "dbGenerated") {
      override def nameParser = LiteralNames
      //JdbcTypeInfo => Option[ClassTag[_]]
      override def typer: Typer = x => super.typer(x).orElse{
        Option.when(x.typeName.contains("json") || x.typeName.contains("jsonb"))(classTag[String])
      }
    }
    gen.writeFiles("dbGenerated/src/main/scala/dbGenerated")
  }
}
