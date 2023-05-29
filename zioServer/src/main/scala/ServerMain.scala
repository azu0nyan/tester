//import dbGenerated.tester.User
import io.getquill.*
import zio.*

import java.sql.SQLException

object ServerMain {
  def main(args: Array[String]): Unit = {
    import dbGenerated.tester.*


    val ctx = new PostgresJdbcContext[PostgresEscape](PostgresEscape, "databaseConfig")
//    val ctx = new PostgresJdbcContext[CompositeNamingStrategy2[Literal, PostgresEscape]](NamingStrategy(Literal, PostgresEscape), "databaseConfig")
    import ctx._

    inline def q = quote{
      query[RegisteredUser]
    }

    val list: List[RegisteredUser] =  run(q)

    println(list)
  }
}
