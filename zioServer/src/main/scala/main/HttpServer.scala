package main

import zio.*
import zio.http.*
import zio.http.HttpError.{InternalServerError, NotFound}
import zio.http.Header.{AccessControlAllowMethods, AccessControlAllowOrigin, Origin}
import zio.http.HttpAppMiddleware.cors
import zio.http.internal.middlewares.Cors.CorsConfig
import clientRequests.{CourseNotOwnedByYou, Route}
import clientRequests.*
import clientRequests.admin.{AddCourseToGroup, AddCourseToGroupRequest}
import tester.srv.controller.{Application, PublicApp}
import tester.srv.controller.impl.ApplicationImpl

object HttpServer {

  type HttpServerContext = PublicApp


  // Create CORS configuration
  val config: CorsConfig =
    CorsConfig(
      allowedOrigin = _ => Some(AccessControlAllowOrigin.All),
      allowedMethods = AccessControlAllowMethods(Method.PUT, Method.DELETE, Method.GET, Method.POST),
    )


  import pureconfig._
  import pureconfig.generic.derivation.default._

  final case class HttpServerConfig(
                                     port: Int,
                                   )derives ConfigReader


  val httpServerConfig = ZIO.succeed {
    val httpConfig: ConfigReader.Result[HttpServerConfig] = Configs.config.at("http").load[HttpServerConfig]
    if (httpConfig.isLeft) println(httpConfig) //todo log better
    httpConfig.right.get
  }


  def makeHttpFromRoute[REQ, RES](route: Route[REQ, RES], func: REQ => ZIO[Any, Throwable, RES]
                                 ): Http[Any, Response, Request, Response] =
    Http.collectZIO[Request] {
      case req@(Method.POST -> Root / route.route) =>
        (for {
          body <- req.body.asString
          req = route.decodeRequest(body)
          res <- func(req)
        } yield Response.json(route.encodeResponse(res)))
          .tapErrorCause(err => ZIO.logErrorCause(s"""Error processing request to '${route.route}'"""", err))
          .mapError(err => Response.fromHttpError(InternalServerError(err.getMessage, Some(err))))
          .tapDefect(err => ZIO.logErrorCause(s"Defect when processing request to ${route.route}", err))
          .catchAllDefect(t => ZIO.succeed(Response.fromHttpError(InternalServerError())))
    }
  def httpFromSeq(seq: Http[Any, Response, Request, Response]*): Http[Any, Response, Request, Response] =
    seq.reduce(_ ++ _)

  def httpServer(a: HttpServerContext): Http[Any, Response, Request, Response] =
    httpFromSeq(
      makeHttpFromRoute(CourseData, req => a.courseData(req)),
      makeHttpFromRoute(CoursesList, req => a.coursesList(req)),
      makeHttpFromRoute(PartialCourseData, req => a.partialCourseData(req)),
      makeHttpFromRoute(ProblemData, req => a.problemData(req)),
      makeHttpFromRoute(UserData, req => a.userData(req)),
      makeHttpFromRoute(Login, req => a.login(req)),
      makeHttpFromRoute(Registration, req => a.registration(req)),
      makeHttpFromRoute(StartCourse, req => a.startCourse(req)),
      makeHttpFromRoute(SubmitAnswer, req => a.submitAnswer(req)),
      makeHttpFromRoute(UpdateUserData, req => a.updateUserData(req)),

      makeHttpFromRoute(watcher.GroupScores, req => a.groupScores(req)),
      makeHttpFromRoute(watcher.LightGroupScores, req => a.lightGroupScores(req)),

      makeHttpFromRoute(teacher.AnswerForConfirmationList, req => a.answerForConfirmationList(req)),
      makeHttpFromRoute(teacher.AnswersList, req => a.answersList(req)),
      makeHttpFromRoute(teacher.ModifyProblem, req => a.modifyProblem(req)),
      makeHttpFromRoute(teacher.TeacherConfirmAnswer, req => a.teacherConfirmAnswer(req)),

      makeHttpFromRoute(admin.AddCourseToGroup, req => a.addCourseToGroup(req)),
      makeHttpFromRoute(admin.AddProblemToCourseTemplate, req => a.addProblemToCourseTemplate(req)),
      makeHttpFromRoute(admin.AddUserToGroup, req => a.addUserToGroup(req)),
      makeHttpFromRoute(admin.AdminAction, req => a.adminAction(req)),
      makeHttpFromRoute(admin.AdminCourseInfo, req => a.adminCourseInfo(req)),
      makeHttpFromRoute(admin.AdminCourseList, req => a.adminCourseList(req)),
      makeHttpFromRoute(admin.GroupInfo, req => a.groupInfo(req)),
      makeHttpFromRoute(admin.GroupList, req => a.groupList(req)),
      makeHttpFromRoute(admin.NewCourseTemplate, req => a.newCourseTemplate(req)),
      makeHttpFromRoute(admin.NewGroup, req => a.newGroup(req)),
      makeHttpFromRoute(admin.ProblemTemplateList, req => a.problemTemplateList(req)),
      makeHttpFromRoute(admin.RemoveCustomProblemTemplate, req => a.removeCustomProblemTemplate(req)),
      makeHttpFromRoute(admin.RemoveProblemFromCourseTemplate, req => a.removeProblemFromCourseTemplate(req)),
      makeHttpFromRoute(admin.RemoveUserFromGroup, req => a.removeUserFromGroup(req)),
      makeHttpFromRoute(admin.UpdateCustomCourse, req => a.updateCustomCourse(req)),
      makeHttpFromRoute(admin.UpdateCustomProblemTemplate, req => a.updateCustomProblemTemplate(req)),
      makeHttpFromRoute(admin.UserList, req => a.userList(req)),
    ) @@ cors(config)


  def startServer(port: Int): ZIO[HttpServerContext, Any, Any] = for {
    config <- httpServerConfig
    _ <- ZIO.log(s"Starting server at port ${config.port}")
    app <- ZIO.service[HttpServerContext]
    _ <- Server.serve(httpServer(app)).provide(Server.defaultWithPort(config.port))
  } yield ()

}
