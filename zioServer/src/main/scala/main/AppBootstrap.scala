package main

import zio.*
import io.github.gaelrenoux.tranzactio.doobie.*
import doobie.util.transactor
import io.github.gaelrenoux.tranzactio.doobie.*
import io.github.gaelrenoux.tranzactio.{DatabaseOps, DbException, ErrorStrategiesRef}
import main.HttpServer.HttpServerContext
import tester.srv.controller.Application
import tester.srv.controller.impl.ApplicationImpl


object AppBootstrap {

  val datasource = ConnectionPool.layer
  val database: ZLayer[Any, Throwable, Database] = datasource >>> Database.fromDatasource
  val layer: ZLayer[Any, Throwable, Application] = database >>> ApplicationImpl.layer


}
