package dbMigration

import controller.UserRole
import controller.db.CollectionOps

import java.util.concurrent.atomic.AtomicInteger
import controller.UserRole._

import dbMigration.DbJsonMappings._
import io.getquill._
import io.getquill.context.json.PostgresJsonExtensions

import java.io.File
import java.time.{Instant, LocalDateTime, ZoneOffset}

object Main {
  //  case class RegisteredUser(id: Int, login: String, passwordHash: String, passwordSalt: String, firstName: Option[String], lastName: Option[String], email: Option[String], registeredAt: java.time.LocalDateTime, lastLogin: Option[java.time.LocalDateTime], role: String)

  val ctx = new PostgresJdbcContext[PostgresEscape](PostgresEscape, "databaseConfig") with PostgresJsonExtensions

  import ctx._

  case class RegisteredUser(id: Int, login: String, passwordHash: String, passwordSalt: String,
                            firstName: Option[String], lastName: Option[String], email: Option[String],
                            registeredAt: java.time.LocalDateTime, lastLogin: Option[java.time.LocalDateTime],
                            role: JsonbValue[UserRole])


  def main(args: Array[String]): Unit = try {
    migrateUsers()
  } finally {
    case t: Throwable =>
      t.printStackTrace()
      ctx.close()
  }

  var uidMap: Map[String, Int] = Map()

  def migrateUsers(): Unit = {


    val uids = new AtomicInteger(0)
    val allUsers = controller.db.users.all()
    println(s"Found ${allUsers.size} users.")

    uidMap = allUsers.map(u => (u._id.toHexString, uids.getAndIncrement())).toMap

    for (u <- allUsers) {
      println(u)
      val toIns = RegisteredUser(uidMap(u._id.toHexString), u.login, u.passwordHash, u.passwordSalt,
        u.firstName, u.lastName, u.email,
        LocalDateTime.ofInstant(u.registeredAt.getOrElse(Instant.now()), ZoneOffset.UTC),
        u.lastLogin.map(l => LocalDateTime.ofInstant(l, ZoneOffset.UTC)),
        JsonbValue(u.role))
      /*
            val q = quote {
              query[RegisteredUser].insertValue(ctx.lift(toIns))
            } //?::jsonb
      */

      val req =
        quote { (ru: RegisteredUser) =>
          sql"""INSERT INTO "RegisteredUser" ("id","login","passwordHash","passwordSalt","firstName","lastName","email","registeredAt","lastLogin","role")
               VALUES (${ru.id}, ${ru.login}, ${ru.passwordHash}, ${ru.passwordSalt}, ${ru.firstName}, ${ru.lastName}, ${ru.email},
               ${ru.registeredAt}, ${ru.lastLogin}, ${ru.role}::jsonb)"""
            .as[Insert[RegisteredUser]]
        }

      val qr = req

      ctx.run(qr(lift(toIns)))
    }


  }
}
