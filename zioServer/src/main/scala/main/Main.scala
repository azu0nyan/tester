package main


import zio.ZIOAppDefault

object Main extends ZIOAppDefault{
  override def run = HttpServer.startServer.provideLayer(AppBootstrap.layer)
}
