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


object DoobieGrpcMain extends ZIOAppDefault {
  //todo add PostgresJdbcLayer
  val doobieContext = new PostgresJdbcContext[PostgresEscape](PostgresEscape, "databaseConfig")

  def services = ServiceList.add(DoobieUserService.transformContextZIO((rc: RequestContext) => Z.succeed(doobieContext)))

  def port: Int = 9000

  def welcome: Z[Any, Throwable, Unit] =
    printLine("Server is running. Press Ctrl-C to stop.")

  def smth: Z[ZUserService[DoobieServiceContext], Throwable, Unit] =
    for {
      s <- Z.service[ZUserService[DoobieServiceContext]]
      r <- s.userList(UserListRequest(), doobieContext)
      _ <- Console.printLine(r)
      l1 <- s.checkFreeLogin(CheckFreeLoginRequest(login = "azu"), doobieContext)
      l2 <- s.checkFreeLogin(CheckFreeLoginRequest(login = "az"), doobieContext)
      _ <- Console.printLine(l1)
      _ <- Console.printLine(l2)
    } yield ()


  def builder = ServerBuilder.forPort(port).addService(ProtoReflectionService.newInstance())

  def serverLive: ZLayer[Any, Throwable, Server] = ServerLayer.fromServiceList(builder, services)

  val myAppLogic = welcome *>
    smth.provideSomeLayer(ZLayer.succeed(DoobieUserService)) *>
    serverLive.launch

  def run = myAppLogic.exitCode
  //  def run = ZIO.succeed(()).exit
}

