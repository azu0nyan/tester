package main
import io.getquill.{PostgresEscape, PostgresJdbcContext}
import scalapb.zio_grpc.*
import grpc_api.user_api.{CheckFreeLoginRequest, CheckFreeLoginResponse, UserInfo, UserListRequest, UserListResponse}
import grpc_api.user_api.ZioUserApi.ZUserService
import zio.*
import zio.Console.*
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService


object GrpcMain extends ZIOAppDefault {
  //todo add PostgresJdbcLayer

  def postgresLive: ZLayer[Any, Throwable, PostgresUserServiceContext] = ZLayer.succeed{
    new PostgresJdbcContext[PostgresEscape](PostgresEscape, "databaseConfig")
  }
//
//  def services = ServiceList.add(PostgresUserService)
  def services = ServiceList.add(DummyUserService)
//
  def port: Int = 9000

  def welcome: ZIO[Any, Throwable, Unit] =
    printLine("Server is running. Press Ctrl-C to stop.")

  def smth: ZIO[ZUserService[Any], Throwable, Unit] =
    for {
      s <- ZIO.service[ZUserService[Any]]
      r <- s.userList(UserListRequest(), ())
      _ <- Console.print(r)
    } yield ()


  def builder = ServerBuilder.forPort(port).addService(ProtoReflectionService.newInstance())

  def serverLive: ZLayer[Any, Throwable, Server] = ServerLayer.fromServiceList(builder, services)

  val myAppLogic = welcome *> (smth.provideLayer(ZLayer.succeed(DummyUserService))) *> serverLive.launch

  def run = myAppLogic.exitCode
//  def run = ZIO.succeed(()).exit
}
