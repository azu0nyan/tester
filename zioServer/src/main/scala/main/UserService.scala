package main

import com.google.protobuf.empty.Empty
import grpc_api.user_api.{CheckFreeLoginRequest, CheckFreeLoginResponse, UserInfo, UserListRequest, UserListResponse}
import grpc_api.user_api.ZioUserApi.ZUserService
import io.getquill.PostgresJdbcContext
import io.getquill.context.jdbc.JdbcContext
import io.grpc.StatusException
import zio.*
import io.getquill.*

type UserServiceContext = PostgresJdbcContext[_]

object UserService extends ZUserService[UserServiceContext]{
  override def userList(request: UserListRequest, context: UserServiceContext): IO[StatusException, UserListResponse] = {
    import context.*
    case class RegisteredUser(login: String)

    inline def q = quote {
      query[RegisteredUser]
    }

    val list: List[UserInfo] = run(q).map(u => UserInfo(u.login, "name", "last"))
    
    ZIO.succeed(UserListResponse.of(list))
  }

  override def checkFreeLogin(request: CheckFreeLoginRequest, context: UserServiceContext): IO[StatusException, CheckFreeLoginResponse] = ???

  override def loggedInUserStream(request: Empty, context: UserServiceContext): stream.Stream[StatusException, UserInfo] = ???
}
