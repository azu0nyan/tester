package main


import doobie.util.transactor
import scalapb.zio_grpc.*
import grpc_api.user_api.{CheckFreeLoginRequest, CheckFreeLoginResponse, UserInfo, UserListRequest, UserListResponse}
import grpc_api.user_api.ZioUserApi.ZUserService
import zio.*
import zio.ZIO as Z
import zio.Console.*
import io.grpc.{ServerBuilder, Status, StatusException}
import io.grpc.protobuf.services.ProtoReflectionService
import io.github.gaelrenoux.tranzactio.doobie.*
import io.github.gaelrenoux.tranzactio.{DatabaseOps, DbException, ErrorStrategiesRef}

object DoobieGrpcMain extends ZIOAppDefault {
  //todo add PostgresJdbcLayer

  val datasource = ConnectionPool.live
  val database: ZLayer[Any, Throwable, DatabaseOps.ServiceOps[transactor.Transactor[Task]]] = datasource >>> Database.fromDatasource
  //  val doobieContext: DoobieServiceContext = database

  def services = ServiceList.add(
    DoobieUserService.transformContext(c => database)
  )
  //    DoobieUserService.transformContext(c => database)

  def port: Int = 9000

  def welcome: Z[Any, Throwable, Unit] =
    printLine("Server is running. Press Ctrl-C to stop.")

  def smth: Z[ZUserService[DoobieCtx], Throwable, Unit] =
    for {
      _ <- Console.printLine("Running..")
      s <- Z.service[ZUserService[DoobieCtx]]
      _ <- Console.printLine("Running.....")
      r <- s.userList(UserListRequest(), database).tapError(err => Console.printLine(err))
      _ <- Console.printLine(s"user list $r")
      l1 <- s.checkFreeLogin(CheckFreeLoginRequest(login = "azu"), database)
      l2 <- s.checkFreeLogin(CheckFreeLoginRequest(login = "az"), database)
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

