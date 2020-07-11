package app
import clientRequests.{LoginRequest, LoginSuccessResponse}
import constants.Skeleton
import controller.{LoginUser, RegisterUser}
import spark._
import spark.Spark._
import viewData.UserViewData
object HttpServer {

  def initRoutesAndStart(): Unit ={
    externalStaticFileLocation("workdir")
//    println(Spark.staticFiles.)
    port(8080)
    get("/", (request: Request, response: Response) => {Skeleton()})

    addRoute(clientRequests.Login, LoginUser.loginUser)
    addRoute(clientRequests.Registration, RegisterUser.registerUser)
  }


  def addRoute[REQ, RES](reqRes:clientRequests.Route[REQ, RES], action: REQ => RES):Unit  =
    post(reqRes.route,  (request: Request, response: Response) => {
      val req:REQ = reqRes.decodeRequest(request.body())
      val res:RES = action(req)
      val resStr = reqRes.encodeResponse(res)
      resStr
    })
}
