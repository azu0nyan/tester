package main

import com.google.protobuf.empty.Empty
import grpc_api.user_api.{CheckFreeLoginRequest, CheckFreeLoginResponse, LoginRequest, LoginResponse, RegistrationRequest, RegistrationResponse, UserDataRequest, UserInfo, UserListRequest, UserListResponse, UserViewData}
import grpc_api.user_api.ZioUserApi.ZUserService
import io.grpc.StatusException
import zio.{IO, stream}

type DoobieServiceContext = Unit

object DoobieUserService extends ZUserService[DoobieServiceContext]{
  override def register(request: RegistrationRequest, context: DoobieServiceContext): IO[StatusException, RegistrationResponse] = ???

  override def logIn(request: LoginRequest, context: DoobieServiceContext): IO[StatusException, LoginResponse] = ???

  override def getUserData(request: UserDataRequest, context: DoobieServiceContext): IO[StatusException, UserViewData] = ???

  override def userList(request: UserListRequest, context: DoobieServiceContext): IO[StatusException, UserListResponse] = ???

  override def checkFreeLogin(request: CheckFreeLoginRequest, context: DoobieServiceContext): IO[StatusException, CheckFreeLoginResponse] = ???

  override def loggedInUserStream(request: Empty, context: DoobieServiceContext): stream.Stream[StatusException, UserInfo] = ???
}
