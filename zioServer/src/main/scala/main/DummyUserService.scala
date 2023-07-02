package main

import com.google.protobuf.empty.Empty
import grpc_api.user_api.{CheckFreeLoginRequest, CheckFreeLoginResponse, UserInfo, UserListRequest, UserListResponse}
import grpc_api.user_api.ZioUserApi.ZUserService
import io.grpc.StatusException
import zio.ZIO as Z
import zio.*

//todo move to tests
object DummyUserService extends ZUserService[Any]{
  val users = Seq(
    UserInfo(login = "alex22",firstName =  "Alex", lastName =  "Twos"),
    UserInfo(login = "tImmy",firstName =  "Tom", lastName =  "Inny"),
    UserInfo(login = "r0201",firstName =  "Romm", lastName =  "Zero"),
  )
  
  override def userList(request: UserListRequest, context: Any): IO[StatusException, UserListResponse] =
    Z.succeed(UserListResponse(users = users))    

  override def checkFreeLogin(request: CheckFreeLoginRequest, context: Any): IO[StatusException, CheckFreeLoginResponse] =
    Z.succeed(CheckFreeLoginResponse(isFree = !users.exists(_.login == request.login)))

  override def loggedInUserStream(request: Empty, context: Any): stream.Stream[StatusException, UserInfo] =
    stream.ZStream.empty
}
