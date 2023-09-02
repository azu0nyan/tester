package main

import zio.*
import io.github.gaelrenoux.tranzactio.doobie.*
import doobie.util.transactor
import io.github.gaelrenoux.tranzactio.doobie.*
import io.github.gaelrenoux.tranzactio.{DatabaseOps, DbException, ErrorStrategiesRef}
import main.HttpServer.HttpServerContext
import tester.srv.controller.Application
import tester.srv.controller.impl.ApplicationImpl


object FullApp extends ZIOAppDefault{

  val datasource = ConnectionPool.layer
  val database: ZLayer[Any, Throwable, Database] = datasource >>> Database.fromDatasource
  val app: ZLayer[Any, Throwable, Application] = database >>> ApplicationImpl.layer

  override def run = HttpServer.startServer.provideLayer(app)
}
