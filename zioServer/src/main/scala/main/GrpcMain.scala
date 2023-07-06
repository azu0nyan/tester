package main

import io.getquill.{PostgresEscape, PostgresJdbcContext}
import scalapb.zio_grpc.*
import grpc_api.user_api.{CheckFreeLoginRequest, CheckFreeLoginResponse, UserInfo, UserListRequest, UserListResponse}
import grpc_api.user_api.ZioUserApi.ZUserService
import zio.*
import zio.ZIO as Z
import zio.Console.*
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService


object GrpcMain extends ZIOAppDefault {
  //todo add PostgresJdbcLayer
  val postgresContext = new PostgresJdbcContext[PostgresEscape](PostgresEscape, "databaseConfig")

  def postgresLive: ZLayer[Any, Throwable, PostgresUserServiceContext] = ZLayer.succeed {
    postgresContext
  }

  //
    def services = ServiceList.add(PostgresUserService.transformContextZIO((rc: RequestContext) => Z.succeed(postgresContext)))
//  def services = ServiceList.add(DummyUserService)

  //
  def port: Int = 9000

  def welcome: Z[Any, Throwable, Unit] =
    printLine("Server is running. Press Ctrl-C to stop.")

  def smth: Z[ZUserService[PostgresUserServiceContext], Throwable, Unit] =
    for {
      s <- Z.service[ZUserService[PostgresUserServiceContext]]
      r <- s.userList(UserListRequest(), postgresContext)
      _ <- Console.print(r)
    } yield ()


  def builder = ServerBuilder.forPort(port).addService(ProtoReflectionService.newInstance())

  def serverLive: ZLayer[Any, Throwable, Server] = ServerLayer.fromServiceList(builder, services)

  val myAppLogic = welcome *>
    smth.provideSomeLayer(ZLayer.succeed(PostgresUserService)) *>
    serverLive.launch

  def run = myAppLogic.exitCode
  //  def run = ZIO.succeed(()).exit
}
