package main


import zio.{ZEnvironment, ZIOAppDefault}

object Main extends ZIOAppDefault{
  override def run = HttpServer.startServer.provideLayer(AppBootstrap.layer.map(l => ZEnvironment.apply(l.get._2)))
}
