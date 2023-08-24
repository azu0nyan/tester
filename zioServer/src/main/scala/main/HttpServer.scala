package main

import zio.*
import zio.http.*
import zio.http.HttpError.InternalServerError

object HttpServer extends ZIOAppDefault {

  val app: Http[Any, Response, Request, Response] = Http.collectZIO[Request]{
    case req@(Method.POST -> Root / "zalupa") =>
      for{
        body <- req.body.asString
      } yield Response.text("KEK" + body)
  }.mapError(err => Response.fromHttpError(InternalServerError()))


  val app2 = Handler.text("LOL KEK").toHttp
//  val app2 = Handler.text("LOL KEK").toHttp

  override def run =
    Server.serve(app ++ app2).provide(Server.defaultWithPort(8080))
}
