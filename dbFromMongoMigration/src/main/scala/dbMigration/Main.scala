package dbMigration
import controller.UserRole
import controller.db.CollectionOps

import java.util.concurrent.atomic.AtomicInteger
import io.circe.syntax._
import controller.UserRole._
import io.circe._
import io.circe.generic.semiauto._
import io.getquill._

import java.io.File
import java.time.{Instant, LocalDateTime, ZoneOffset}

object Main {
//  case class RegisteredUser(id: Int, login: String, passwordHash: String, passwordSalt: String, firstName: Option[String], lastName: Option[String], email: Option[String], registeredAt: java.time.LocalDateTime, lastLogin: Option[java.time.LocalDateTime], role: String)

  val ctx = new PostgresJdbcContext[PostgresEscape](PostgresEscape, "databaseConfig")
  import ctx._

  def main(args: Array[String]): Unit = {
    migrateUsers()
  }

  var uidMap: Map[String, Int] = Map()

  def migrateUsers(): Unit = {
    case class RegisteredUser(id: Int, login: String, passwordHash: String, passwordSalt: String,
                              firstName: Option[String], lastName: Option[String], email: Option[String],
                              registeredAt: java.time.LocalDateTime, lastLogin: Option[java.time.LocalDateTime],
                              role: String)


    val uids = new AtomicInteger()
    println(new File("").toPath.toAbsolutePath.toString)
    val allUsers = controller.db.users.all()
    println(s"Found ${allUsers.size} users.")
    uidMap = allUsers.map(u => (u._id.toHexString, uids.getAndIncrement())).toMap

     for(u <- allUsers) {
      val toIns = RegisteredUser(uidMap(u._id.toHexString), u.login, u.passwordHash, u.passwordSalt,
        u.firstName, u.lastName, u.email,
        LocalDateTime.ofInstant(u.registeredAt.getOrElse(Instant.now()), ZoneOffset.UTC),
        u.lastLogin.map(l => LocalDateTime.ofInstant(l, ZoneOffset.UTC)),
        u.role.asJson.noSpaces)

      val q = quote{
        query[RegisteredUser].insertValue(ctx.lift(toIns))
      }

      ctx.run(q)
    }


  }
}
