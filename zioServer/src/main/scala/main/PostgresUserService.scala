package main

import com.google.protobuf.empty.Empty
import grpc_api.user_api.{CheckFreeLoginRequest, CheckFreeLoginResponse, LoginRequest, LoginResponse, RegistrationRequest, RegistrationResponse, UserDataRequest, UserInfo, UserListRequest, UserListResponse, UserViewData}
import grpc_api.user_api.ZioUserApi.ZUserService
import io.getquill.PostgresJdbcContext
import io.getquill.context.jdbc.JdbcContext
import io.grpc.{Status, StatusException}
import zio.*
import io.getquill.*

type PostgresUserServiceContext = PostgresJdbcContext[PostgresEscape]


object PostgresUserService extends ZUserService[PostgresUserServiceContext] {
  override def userList(request: UserListRequest, context: PostgresUserServiceContext): IO[StatusException, UserListResponse] =
    ZIO.attemptBlocking {
      import context.*
      case class RegisteredUser(login: String, firstName: String, lastName: String)

      inline def q = quote {
        query[RegisteredUser]
      }
      val list: List[UserInfo] = run(q)
        .map(u => UserInfo(u.login, u.firstName, u.lastName))

      UserListResponse.of(list)
    }.mapError(_ => new StatusException(Status.UNKNOWN))

  override def checkFreeLogin(request: CheckFreeLoginRequest, context: PostgresUserServiceContext): IO[StatusException, CheckFreeLoginResponse] =
    ZIO.attemptBlocking {
      import context.*
      case class Exists(exists: Boolean)
      inline def q = quote{(name: String) =>
        sql"""SELECT EXISTS(SELECT * FROM tester."RegisteredUser" WHERE login ILIKE ${name})"""
          .as[Query[Exists]]
      }

      val res:List[Exists] = run(q(context.lift(request.login)))

      CheckFreeLoginResponse(!res.head.exists)
    }.mapError(_ => new StatusException(Status.UNKNOWN))

  override def loggedInUserStream(request: Empty, context: PostgresUserServiceContext): stream.Stream[StatusException, UserInfo] = ???
  override def register(request: RegistrationRequest, context: PostgresUserServiceContext): IO[StatusException, RegistrationResponse] = ???
  override def logIn(request: LoginRequest, context: PostgresUserServiceContext): IO[StatusException, LoginResponse] = ???
  override def getUserData(request: UserDataRequest, context: PostgresUserServiceContext): IO[StatusException, UserViewData] = ???
}
