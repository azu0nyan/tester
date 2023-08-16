package tester.srv.dao


import io.github.gaelrenoux.tranzactio.doobie.{TranzactIO, tzio}
import CourseTemplateProblemDao.CourseTemplateProblem
import zio.schema.{DeriveSchema, Schema}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import RegisteredUserDao.RegisteredUser

import java.time.Instant


object RegisteredUserDao extends AbstractDao [RegisteredUser]{
  case class RegisteredUser(id: Long, login: String, firstName: String, lastName: String, email: String,
                            passwordHash: String, passwordSalt: String, registeredAt: Instant, role: String)

  override val schema: Schema[RegisteredUser] = DeriveSchema.gen[RegisteredUser]
  override val tableName: String = "RegisteredUser"
  override val jsonbFields: Seq[String] = Seq("role")

  def byLogin(login: String): TranzactIO[Option[RegisteredUser]] = 
    selectWhereOption(fr"login ILIKE ${login}")


  def loginExists(login: String): TranzactIO[Boolean] =
    case class Exists(exists: Boolean)
    tzio {
      sql"""SELECT EXISTS(SELECT * FROM RegisteredUser where login ILIKE ${login})"""
        .query[Exists].unique.map(_.exists)
    }

}

