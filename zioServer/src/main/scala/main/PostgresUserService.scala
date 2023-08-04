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

      val list: List[UserInfo] = run(query[RegisteredUser])
        .map(u => UserInfo(u.login, u.firstName, u.lastName))

      UserListResponse.of(list)
    }.mapError(_ => new StatusException(Status.UNKNOWN))


  case class LoginExists(exists: Boolean)
  inline def loginExists = quote { (name: String) =>
    sql"""SELECT EXISTS(SELECT * FROM tester."RegisteredUser" WHERE login ILIKE ${name})"""
      .as[Query[LoginExists]]
  }

  override def checkFreeLogin(request: CheckFreeLoginRequest, context: PostgresUserServiceContext): IO[StatusException, CheckFreeLoginResponse] =
    ZIO.attemptBlocking {
      import context.*
      val res: List[LoginExists] = run(loginExists(context.lift(request.login)))
      CheckFreeLoginResponse(!res.head.exists)
    }.mapError(_ => new StatusException(Status.UNKNOWN))

  override def register(request: RegistrationRequest, context: PostgresUserServiceContext): IO[StatusException, RegistrationResponse] =
    ZIO.attemptBlocking {
      import context.*

//      run(sql"SET TRANSACTION ISOLATION LEVEL SERIALIZABLE".as[Action[_]])

      class LoginExistsException extends Exception

      transaction {
        val res = run(loginExists(context.lift(request.login)))
        if(res.size == 1 && res.head.exists) {

        } else {
          throw new LoginExistsException()
        }
      }

      RegistrationResponse(RegistrationResponse.Result.Success(Empty()))
    }.mapError(_ => new StatusException(Status.UNKNOWN))

  override def logIn(request: LoginRequest, context: PostgresUserServiceContext): IO[StatusException, LoginResponse] = ???
  override def getUserData(request: UserDataRequest, context: PostgresUserServiceContext): IO[StatusException, UserViewData] = ???
  override def loggedInUserStream(request: Empty, context: PostgresUserServiceContext): stream.Stream[StatusException, UserInfo] = ???
}
