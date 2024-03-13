package main


import zio.{ZEnvironment, ZIOAppDefault}

object Main extends ZIOAppDefault{
  override def run = HttpServer.startServer(3228).provideLayer(AppBootstrap.layer.map(l => ZEnvironment.apply(l.get._2)))
}
