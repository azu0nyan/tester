package main

import com.google.protobuf.empty.Empty
import grpc_api.user_api.{CheckFreeLoginRequest, CheckFreeLoginResponse, LoginRequest, LoginResponse, RegistrationRequest, RegistrationResponse, UserDataRequest, UserInfo, UserListRequest, UserListResponse, UserViewData}
import grpc_api.user_api.ZioUserApi.ZUserService
import io.grpc.{Status, StatusException}
import zio.*
import doobie.implicits.*
import io.github.gaelrenoux.tranzactio.doobie
import io.github.gaelrenoux.tranzactio.doobie.{Connection, Database, TranzactIO, tzio}

import javax.sql.DataSource


type DoobieServiceContext = ZLayer[Any, Throwable, Database]

object DoobieUserService extends ZUserService[DoobieServiceContext] {
  override def register(request: RegistrationRequest, context: DoobieServiceContext): IO[StatusException, RegistrationResponse] = ???

  override def logIn(request: LoginRequest, context: DoobieServiceContext): IO[StatusException, LoginResponse] = ???

  override def getUserData(request: UserDataRequest, context: DoobieServiceContext): IO[StatusException, UserViewData] = ???

  override def userList(request: UserListRequest, context: DoobieServiceContext): IO[StatusException, UserListResponse] =
    (for {
      _ <- Console.printLine(s"Getting user list")
      db <- ZIO.service[Database]
      _ <- Console.printLine(s"Got service")
      res <- db.transactionOrWiden(UserQuery.userList)
      _ <- Console.printLine(s"Got users")
    } yield UserListResponse.of(res.map(r => UserInfo(r.login, r.firstName, r.lastName))))
      .provideLayer(context)
      .tapError(err => Console.printLine(err))
      .tapDefect(err => Console.printLine(err))
      .mapError(_ => new StatusException(Status.UNKNOWN))


  override def checkFreeLogin(request: CheckFreeLoginRequest, context: DoobieServiceContext): IO[StatusException, CheckFreeLoginResponse] = ???

  override def loggedInUserStream(request: Empty, context: DoobieServiceContext): stream.Stream[StatusException, UserInfo] = ???
}

case class RegisteredUser(login: String, firstName: String, lastName: String)

object UserQuery {
  def userList: TranzactIO[List[RegisteredUser]] = tzio {
    sql"""SELECT login, "firstName", "lastName" FROM tester."RegisteredUser" """
      .query[RegisteredUser].to[List]
  }
}
