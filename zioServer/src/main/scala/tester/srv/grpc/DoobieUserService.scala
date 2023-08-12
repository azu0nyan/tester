package tester.srv.grpc

import com.google.protobuf.empty.Empty
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import grpc_api.user_api.ZioUserApi.ZUserService
import grpc_api.user_api.*
import grpc_api.user_api.RegistrationFailure.Failure.UserAlreadyExists
import io.github.gaelrenoux.tranzactio.doobie
import io.github.gaelrenoux.tranzactio.doobie.{Connection, Database, TranzactIO, tzio}
import io.grpc.{Status, StatusException}
import tester.srv.controller.{PasswordHashingSalting, UserOps}
import tester.srv.controller.PasswordHashingSalting.HashAndSalt
import tester.srv.controller.UserOps.RegistrationResult
import zio.*

import java.time.Instant
import javax.sql.DataSource


type DoobieCtx = ZLayer[Any, Throwable, Database]

object DoobieUserService extends ZUserService[DoobieCtx] {

  def mapRegistrationResults(r: RegistrationResult): RegistrationResponse =
    import RegistrationResult.*
    import RegistrationResponse.*
    r match
      case Success =>
        RegistrationResponse(Result.Success(com.google.protobuf.empty.Empty()))
      case AlreadyExists(login) =>
        RegistrationResponse(Result.Failure(RegistrationFailure(UserAlreadyExists(login))))
      case LoginToShort(min) =>
        RegistrationResponse(result = Result.Failure(RegistrationFailure(
          RegistrationFailure.Failure.LoginToShort(min.toString))))
      case PasswordToShort(min) =>
        RegistrationResponse(result = Result.Failure(
          RegistrationFailure(RegistrationFailure.Failure.UnknownError("Password to short"))))// todo
      case WrongCharsInLogin =>
        RegistrationResponse(result = Result.Failure(
          RegistrationFailure(RegistrationFailure.Failure.UnknownError("Wrong chars in login")))) // todo
      case ZeroRowsUpdated =>
        RegistrationResponse(result = Result.Failure(
          RegistrationFailure(RegistrationFailure.Failure.UnknownError("Zero rows updated")))) // todo
      case UnknownError(t, msg) =>
        RegistrationResponse(result = Result.Failure(
          RegistrationFailure(RegistrationFailure.Failure.UnknownError(msg.getOrElse(""))))) // todo

  override def register(request: RegistrationRequest, context: DoobieCtx): IO[StatusException, RegistrationResponse] =
    val trans = for {
      db <- ZIO.service[Database]
      res <- db
        .transactionOrWiden(UserOps.registerUser(request))
        .map(mapRegistrationResults)
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
      res <- db.transactionOrWiden(UserOps.userList)
    } yield UserListResponse.of(res.map(r => UserInfo(r.login, r.firstName, r.lastName)))
    trans
      .provideLayer(context)
      .tapError(err => Console.printLine(err))
      .mapError(_ => new StatusException(Status.INTERNAL))

  override def checkFreeLogin(request: CheckFreeLoginRequest, context: DoobieCtx): IO[StatusException, CheckFreeLoginResponse] =
    val trans = for {
      db <- ZIO.service[Database]
      res <- db.transactionOrWiden(UserOps.loginExists(request.login))
    } yield CheckFreeLoginResponse.of(!res)
    trans
      .provideLayer(context)
      .tapError(err => Console.printLine(err))
      .mapError(_ => new StatusException(Status.INTERNAL))


  override def loggedInUserStream(request: Empty, context: DoobieCtx): stream.Stream[StatusException, UserInfo] = ???
}

