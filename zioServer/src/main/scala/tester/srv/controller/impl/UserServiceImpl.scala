package tester.srv.controller.impl


import doobie.Update
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
import tester.srv.controller.{MessageBus, PasswordHashingSalting, TokenOps, UserService}
import tester.srv.controller.UserService.LoginResult.*
import tester.srv.controller.UserService.{LoginData, LoginResult, RegistrationData, RegistrationResult}
import tester.srv.dao.RegisteredUserDao.RegisteredUser
import tester.srv.dao.{RegisteredUserDao, UserSessionDao}
import tester.srv.dao.UserSessionDao.UserSession

import java.time.Instant




case class UserServiceImpl(bus: MessageBus) extends UserService{

  val minLoginLength = 3
  val minPassowdLength = 4

  //todo assign role to new users
  /**Returns userId*/
  private def registerUserQuery(req: RegistrationData): TranzactIO[Int] =
    val HashAndSalt(hash, salt) = PasswordHashingSalting.hashPassword(req.password)
    val user = RegisteredUser(0, req.login, req.firstName, req.lastName, req.email, hash, salt,
      java.time.Clock.systemUTC().instant())
    RegisteredUserDao.insertReturnId(user)
  end registerUserQuery

  def registerUser(req: RegistrationData): TranzactIO[RegistrationResult] =
    if (req.login.length < minLoginLength)
      ZIO.succeed(RegistrationResult.LoginToShort(minLoginLength))
    else if (!req.login.matches("[a-zA-Z0-9]*"))
      ZIO.succeed(RegistrationResult.WrongCharsInLogin)
    else if (req.password.length < minPassowdLength)
      ZIO.succeed(RegistrationResult.PasswordToShort(minPassowdLength))
    else
      for {
        res <- ZIO.ifZIO(RegisteredUserDao.loginExists(req.login))(
          onTrue = ZIO.succeed(RegistrationResult.AlreadyExists(req.login)),
          onFalse = registerUserQuery(req).map { userId =>
            RegistrationResult.Success(userId)
          }
        )
      } yield res


  def loginUser(data: LoginData): TranzactIO[LoginResult] =
    for {
      u <- RegisteredUserDao.byLogin(data.login)
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
              _ <- bus.publish(MessageBus.UserLoggedIn(user.id, start))
            } yield LoggedIn(token)
          else ZIO.succeed(WrongPassword(data.login, data.password))
        case None => ZIO.succeed(UserNotFound(data.login))
    } yield res.asInstanceOf[LoginResult]

  def validateToken(token: String): TranzactIO[TokenOps.ValidationResult] = {
    TokenOps.decodeAndValidateUserToken(token) match
      case TokenOps.TokenValid(id) =>
        for (sessions <- UserSessionDao.getValidUserSessions(id))
          yield if (sessions.exists(_.token == token)) TokenOps.TokenValid(id)
          else TokenOps.InvalidToken
      case other => ZIO.succeed(other)
  }

  def byLogin(login: String): TranzactIO[Option[viewData.UserViewData]] =
    RegisteredUserDao.byLogin(login).map(_.map(_.toViewData))
}

object UserServiceImpl{
  def live: URIO[MessageBus, UserService] =
    for{
      bus <- ZIO.service[MessageBus]
    } yield UserServiceImpl(bus)
    
  def layer: URLayer[MessageBus, UserService] = ZLayer.fromZIO(live)  
}
