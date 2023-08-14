package EmbeddedPG

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.slf4j.LoggerFactory;

object EmbeddedPGTest {
  def main(args: Array[String]): Unit = {
    LoggerFactory.getLogger(classOf[EmbeddedPostgres]).info(s"Starting..")
    val epg = EmbeddedPostgres.builder()
      .setOutputRedirector(ProcessBuilder.Redirect.PIPE)
      .setErrorRedirector(ProcessBuilder.Redirect.PIPE)
      .start()

    val conn = epg.getPostgresDatabase.getConnection
    val res = conn.prepareStatement(s"SELECT * FROM NO_TABLE").executeQuery()
    println(res)
    conn.close()
  }
}
