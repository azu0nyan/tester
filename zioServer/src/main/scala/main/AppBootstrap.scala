package main

import zio.*
import io.github.gaelrenoux.tranzactio.doobie.*
import doobie.util.transactor
import io.github.gaelrenoux.tranzactio.doobie.*
import io.github.gaelrenoux.tranzactio.{DatabaseOps, DbException, ErrorStrategiesRef}
import main.HttpServer.HttpServerContext
import tester.srv.controller.Application
import tester.srv.controller.impl.{ApplicationImpl, SecureApplication}


object AppBootstrap {

  val datasource = ConnectionPool.layer
  val database: ZLayer[Any, Throwable, Database] = datasource >>> Database.fromDatasource
  val registries =  ApplicationImpl.constructRegistries
  val context:ZLayer[Any, Throwable, ApplicationImpl.AppContext] = (database ++ registries) >>> (ApplicationImpl.constructAppServices ++ database)

  type BootstrapedApp = (Application, SecureApplication)
  val live: ZIO[Any, Throwable, BootstrapedApp]  =
    (for{
      app <- ApplicationImpl.liveContext
      sec <- SecureApplication.liveContext.provideSomeLayer(ZLayer.succeed(app))
    } yield (app, sec)).provideLayer(context)

  val layer: ZLayer[Any, Throwable, BootstrapedApp] = ZLayer.fromZIO(live)


}
