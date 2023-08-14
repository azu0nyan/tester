package tester.srv.controller

import doobie.Update
import grpc_api.user_api.RegistrationRequest
import tester.srv.controller.PasswordHashingSalting.HashAndSalt
import zio.*
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import io.github.gaelrenoux.tranzactio.doobie
import doobie.{Connection, Database, TranzactIO, tzio}

import java.time.Instant


object UserOps {
  case class UserFromList(login: String, firstName: String, lastName: String)

  def userList: TranzactIO[List[UserFromList]] = tzio {
    sql"""SELECT login, firstName, lastName FROM RegisteredUser"""
      .query[UserFromList].to[List]
  }

  def loginExists(login: String): TranzactIO[Boolean] =
    case class Exists(exists: Boolean)
    tzio {
      sql"""SELECT EXISTS(SELECT * FROM RegisteredUser where login ILIKE ${login})"""
        .query[Exists].unique.map(_.exists)
    }

  case class RegisteredUser(login: String, firstName: String, lastName: String, email: String,
                            passwordHash: String, passwordSalt: String, registeredAt: Instant, role: String)
  def getUser(login: String): TranzactIO[RegisteredUser] = tzio {
    sql"""SELECT login, firstName, lastName, email, passwordHash, passwordSalt, registeredAt, role FROM RegisteredUser
         WHERE login ILIKE ${login}""".query[RegisteredUser].unique
  }

  //todo assign role to new users
  private def registerUserQuery(req: RegistrationData) = tzio {
    val HashAndSalt(hash, salt) = PasswordHashingSalting.hashPassword(req.password)
    val user = RegisteredUser(req.login, req.firstName, req.lastName, req.email, hash, salt,
      java.time.Clock.systemUTC().instant(), "{ \"Student\": {}}")
    Update[RegisteredUser](
      """INSERT INTO RegisteredUser
         (login, firstName, lastName, email, passwordHash, passwordSalt, registeredAt, role) VALUES
         (?, ?, ?, ?, ?, ?, ?, ?::jsonb)
         """).updateMany(List(user))
  }

  val minLoginLength = 3
  val minPassowdLength = 4

  sealed trait RegistrationResult
  object RegistrationResult {
    case object Success extends RegistrationResult
    sealed trait Fail extends RegistrationResult
    case class AlreadyExists(login: String) extends Fail
    case class LoginToShort(min: Int) extends Fail
    case class PasswordToShort(min: Int) extends Fail
    case object WrongCharsInLogin extends Fail
    case object ZeroRowsUpdated extends Fail
    case class UnknownError(t: Option[Throwable] = None, msg: Option[String] = None) extends Fail
  }
  case class RegistrationData(login: String, password: String, firstName: String, lastName: String, email: String)

  def registerUser(req: RegistrationData): TranzactIO[RegistrationResult] =
    if (req.login.length < minLoginLength)
      ZIO.succeed(RegistrationResult.LoginToShort(minLoginLength))
    else if (!req.login.matches("[a-zA-Z0-9]*"))
      ZIO.succeed(RegistrationResult.WrongCharsInLogin)
    else if (req.password.length < minPassowdLength)
      ZIO.succeed(RegistrationResult.PasswordToShort(minPassowdLength))
    else
      for {
        res <- ZIO.ifZIO(loginExists(req.login))(
          onTrue = ZIO.succeed(RegistrationResult.AlreadyExists(req.login)),
          onFalse = registerUserQuery(req).map { x =>
            if (x > 0) RegistrationResult.Success
            else RegistrationResult.ZeroRowsUpdated
          }
        )
      } yield res
}

