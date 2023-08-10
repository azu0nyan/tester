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


type DoobieServiceContext = Connection

object DoobieUserService extends ZUserService[DoobieServiceContext]{
  override def register(request: RegistrationRequest, context: DoobieServiceContext): IO[StatusException, RegistrationResponse] = ???

  override def logIn(request: LoginRequest, context: DoobieServiceContext): IO[StatusException, LoginResponse] = ???

  override def getUserData(request: UserDataRequest, context: DoobieServiceContext): IO[StatusException, UserViewData] = ???

  override def userList(request: UserListRequest, context: DoobieServiceContext): IO[StatusException, UserListResponse] = {
    UserQuery.userList.provideLayer(ZLayer.succeed(context))
      .mapBoth(
        err => new StatusException(Status.UNKNOWN),
        res =>  UserListResponse.of(res.map(r => UserInfo(r.login, r.firstName, r.lastName)))
      )
  }

  override def checkFreeLogin(request: CheckFreeLoginRequest, context: DoobieServiceContext): IO[StatusException, CheckFreeLoginResponse] = ???

  override def loggedInUserStream(request: Empty, context: DoobieServiceContext): stream.Stream[StatusException, UserInfo] = ???
}

case class RegisteredUser(login: String, firstName: String, lastName: String)

object UserQuery{
  def userList: TranzactIO[List[RegisteredUser]] = tzio {
    sql"""SELECT * FROM RegisteredUser""".query[RegisteredUser].to[List]
  }
}
