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

  lazy val datasource = ConnectionPool.layer
  lazy val database: ZLayer[Any, Throwable, Database] = datasource >>> Database.fromDatasource
  lazy val registries =  ApplicationImpl.constructRegistries
  lazy val context:ZLayer[Any, Throwable, ApplicationImpl.AppContext] = (database ++ registries) >>> (ApplicationImpl.constructAppServices ++ database)

  type BootstrapedApp = (Application, SecureApplication)
  lazy val live: ZIO[Any, Throwable, BootstrapedApp]  =
    (for{
      app <- ApplicationImpl.liveContext
      sec <- SecureApplication.liveContext.provideSomeLayer(ZLayer.succeed(app))
    } yield (app, sec)).provideLayer(context)

  lazy val layer: ZLayer[Any, Throwable, BootstrapedApp] = ZLayer.fromZIO(live)


}
