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
import io.github.gaelrenoux.tranzactio.{DbException, doobie}
import doobie.{Connection, Database, TranzactIO, tzio}
import tester.srv.controller.UserOps.LoginResult.{LoggedIn, UserNotFound, WrongPassword}
import tester.srv.dao.UserSessionDao
import tester.srv.dao.UserSessionDao.UserSession

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

  case class RegisteredUser(id: Long, login: String, firstName: String, lastName: String, email: String,
                            passwordHash: String, passwordSalt: String, registeredAt: Instant, role: String)
  def getUser(login: String): TranzactIO[Option[RegisteredUser]] = tzio {
    sql"""SELECT id, login, firstName, lastName, email, passwordHash, passwordSalt, registeredAt, role FROM RegisteredUser
         WHERE login ILIKE ${login}""".query[RegisteredUser].option
  }

  //todo assign role to new users
  private def registerUserQuery(req: RegistrationData) = tzio {
    val HashAndSalt(hash, salt) = PasswordHashingSalting.hashPassword(req.password)
    val user = RegisteredUser(0, req.login, req.firstName, req.lastName, req.email, hash, salt,
      java.time.Clock.systemUTC().instant(), "{ \"Student\": {}}")
    Update[RegisteredUser](
      """INSERT INTO RegisteredUser
         (id, login, firstName, lastName, email, passwordHash, passwordSalt, registeredAt, role) VALUES
         (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)
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


  case class LoginData(login: String, password: String, sessionLengthSec: Int = 24 * 60 * 60,
                       ip: Option[String] = None, userAgent: Option[String] = None, platform: Option[String] = None, locale: Option[String] = None,
                      )

  sealed trait LoginResult
  object LoginResult {
    final case class LoggedIn(token: String) extends LoginResult
    final case class UserNotFound(login: String) extends LoginResult
    final case class WrongPassword(login: String, password: String) extends LoginResult
  }
  def loginUser(data: LoginData): TranzactIO[LoginResult] =
    for {
      u <- UserOps.getUser(data.login)
      res <- u match
        case Some(user) =>
          val correctPassword = PasswordHashingSalting.checkPassword(data.password, user.passwordHash, user.passwordSalt)
          if (correctPassword)
            val token = TokenOps.generateToken(user.id, data.sessionLengthSec)
            val start = java.time.Clock.systemUTC().instant()
            val end = java.time.Clock.systemUTC().instant().plus(java.time.Duration.ofSeconds(data.sessionLengthSec))
            for {
              _ <- UserSessionDao.insert(UserSession(0, user.id, token, data.ip,
                data.userAgent, data.platform, data.locale, start, end))
            } yield LoggedIn(token)
          else ZIO.succeed(WrongPassword(data.login, data.password))
        case None => ZIO.succeed(UserNotFound(data.login))
    } yield res
  
  def validateToken(token: String): TranzactIO[TokenOps.ValidationResult] = {
    TokenOps.decodeAndValidateUserToken(token) match
      case TokenOps.TokenValid(id) =>
        for (sessions <- UserSessionDao.getValidUserSessions(id))
          yield if (sessions.exists(_.token == token)) TokenOps.TokenValid(id)
          else TokenOps.InvalidToken
      case other => ZIO.succeed(other)
  }
}

