package main

import com.google.protobuf.empty.Empty
import grpc_api.user_api.{CheckFreeLoginRequest, CheckFreeLoginResponse, UserInfo, UserListRequest, UserListResponse}
import grpc_api.user_api.ZioUserApi.ZUserService
import io.getquill.PostgresJdbcContext
import io.getquill.context.jdbc.JdbcContext
import io.grpc.StatusException
import zio.*
import io.getquill.*

type PostgresUserServiceContext = PostgresJdbcContext[PostgresEscape]

object PostgresUserService extends ZUserService[PostgresUserServiceContext]{
  override def userList(request: UserListRequest, context: PostgresUserServiceContext): IO[StatusException, UserListResponse] = {
    import context.*
    case class RegisteredUser(login: String)

    inline def q = quote {
      query[RegisteredUser]
    }

    val list: List[UserInfo] = run(q).map(u => UserInfo(u.login, "name", "last"))

    ZIO.succeed(UserListResponse.of(list))
  }

  override def checkFreeLogin(request: CheckFreeLoginRequest, context: PostgresUserServiceContext): IO[StatusException, CheckFreeLoginResponse] = ???

  override def loggedInUserStream(request: Empty, context: PostgresUserServiceContext): stream.Stream[StatusException, UserInfo] = ???
}
