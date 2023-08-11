package main

import com.google.protobuf.empty.Empty
import grpc_api.user_api.{CheckFreeLoginRequest, CheckFreeLoginResponse, LoginRequest, LoginResponse, RegistrationFailure, RegistrationRequest, RegistrationResponse, UserDataRequest, UserInfo, UserListRequest, UserListResponse, UserViewData}
import grpc_api.user_api.ZioUserApi.ZUserService
import io.grpc.{Status, StatusException}
import zio.*
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import grpc_api.user_api.RegistrationFailure.Failure.{LoginToShort, UserAlreadyExists}
import io.github.gaelrenoux.tranzactio.doobie
import doobie.{Connection, Database, TranzactIO, tzio}
import main.PasswordHashingSalting.HashAndSalt

import java.time.Instant
import javax.sql.DataSource


type DoobieCtx = ZLayer[Any, Throwable, Database]

object DoobieUserService extends ZUserService[DoobieCtx] {

  //  def validateRegistationData(req: RegistrationRequest)

  override def register(request: RegistrationRequest, context: DoobieCtx): IO[StatusException, RegistrationResponse] =
    val trans = for {
      db <- ZIO.service[Database]
      res <- db.transactionOrWiden(UserTransactions.registerUser(request))
    } yield res
    trans
      .provideLayer(context)
      .tapError(err => Console.printLine(err))
      .mapError(_ => new StatusException(Status.INTERNAL))

  override def logIn(request: LoginRequest, context: DoobieCtx): IO[StatusException, LoginResponse] = ???

  override def getUserData(request: UserDataRequest, context: DoobieCtx): IO[StatusException, UserViewData] = ???

  override def userList(request: UserListRequest, context: DoobieCtx): IO[StatusException, UserListResponse] =
    val trans = for {
      db <- ZIO.service[Database]
      res <- db.transactionOrWiden(UserTransactions.userList)
    } yield UserListResponse.of(res.map(r => UserInfo(r.login, r.firstName, r.lastName)))
    trans
      .provideLayer(context)
      .tapError(err => Console.printLine(err))
      .mapError(_ => new StatusException(Status.INTERNAL))

  override def checkFreeLogin(request: CheckFreeLoginRequest, context: DoobieCtx): IO[StatusException, CheckFreeLoginResponse] =
    val trans = for {
      db <- ZIO.service[Database]
      res <- db.transactionOrWiden(UserTransactions.loginExists(request.login))
    } yield CheckFreeLoginResponse.of(!res)
    trans
      .provideLayer(context)
      .tapError(err => Console.printLine(err))
      .mapError(_ => new StatusException(Status.INTERNAL))


  override def loggedInUserStream(request: Empty, context: DoobieCtx): stream.Stream[StatusException, UserInfo] = ???
}


object UserTransactions {
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


  //todo assign role to new users
  private def registerUserQuery(req: RegistrationRequest) = tzio {
    val HashAndSalt(hash, salt) = PasswordHashingSalting.hashPassword(req.password)
    val user = RegisteredUser(req.login, req.firstName, req.lastName, req.email, hash, salt,
      java.time.Clock.systemUTC().instant(), "Student()")
    Update[RegisteredUser](
      """INSERT INTO RegisteredUser
         (login, firstName, lastName, email, passwordHash, passwordSalt, registeredAt) VALUES
         (?, ?, ?, ?, ?, ?, ?)
         """).updateMany(List(user))
  }

  val minLoginLength = 3

  def registerUser(req: RegistrationRequest): TranzactIO[RegistrationResponse] =
    if (req.login.length < minLoginLength)
      ZIO.succeed(RegistrationResponse(result =
        RegistrationResponse.Result.Failure(RegistrationFailure(LoginToShort(req.login)))))
    else if (!req.login.matches("[a-zA-Z0-9]*") || req.password.length < 4)
      ZIO.succeed(RegistrationResponse(result =
        RegistrationResponse.Result.Failure(RegistrationFailure(RegistrationFailure.Failure.UnknownError("")))))
    else
      for {
        res <- ZIO.ifZIO(loginExists(req.login))(
          onTrue = ZIO.succeed(RegistrationResponse(result =
            RegistrationResponse.Result.Failure(RegistrationFailure(UserAlreadyExists(req.login))))),
          onFalse = registerUserQuery(req).map { x =>
            if (x > 0) RegistrationResponse(result = RegistrationResponse.Result.Success(com.google.protobuf.empty.Empty()))
            else  RegistrationResponse(result =
              RegistrationResponse.Result.Failure(RegistrationFailure(RegistrationFailure.Failure.UnknownError(""))))
          }
        )
      } yield res
}
