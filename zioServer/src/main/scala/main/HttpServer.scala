package main

import zio.*
import zio.http.*
import zio.http.HttpError.InternalServerError
import clientRequests.{CourseNotOwnedByYou, Route}
import clientRequests.*
import clientRequests.admin.{AddCourseToGroup, AddCourseToGroupRequest}

object HttpServer extends ZIOAppDefault {

  type AppContext = Any

  def makeHttpFromRoute[REQ, RES](route: Route[REQ, RES], func: REQ => ZIO[AppContext, Nothing, RES]
                                  ): Http[Any, Response, Request, Response] =
    Http.collectZIO[Request]{
      case req@(Method.POST -> Root / route.route) =>
        (for{
          body <- req.body.asString
          req = route.decodeRequest(body)
          res <- func(req)
        } yield Response.json(route.encodeResponse(res)))
          .mapError(err => Response.fromHttpError(InternalServerError()))
          .catchAllDefect(t =>
            for{
              _ <- ZIO.logError(s"Error processing request to ${route.route} $t")
            } yield Response.fromHttpError(InternalServerError())
          )

    }
  def httpFromSeq(seq:Http[Any, Response, Request, Response]*): Http[Any, Response, Request, Response] =
    seq.reduce(_ ++ _)

  val app: Http[Any, Response, Request, Response] =
    httpFromSeq(
      makeHttpFromRoute(CourseData, x => ZIO.succeed(CourseNotOwnedByYou())),
      makeHttpFromRoute(CoursesList, x => ???),
      makeHttpFromRoute(PartialCourseData, x => ???),
      makeHttpFromRoute(ProblemData, x => ???),
      makeHttpFromRoute(UserData, x => ???),
      makeHttpFromRoute(Login, x => ???),
      makeHttpFromRoute(Registration, x => ???),
      makeHttpFromRoute(StartCourse, x => ???),
      makeHttpFromRoute(SubmitAnswer, x => ???),
      makeHttpFromRoute(UpdateUserData, x => ???),

      makeHttpFromRoute(watcher.GroupScores, x => ???),
      makeHttpFromRoute(watcher.LightGroupScores, x => ???),

      makeHttpFromRoute(teacher.AnswerForConfirmationList, x => ???),
      makeHttpFromRoute(teacher.AnswersList, x => ???),
      makeHttpFromRoute(teacher.ModifyProblem, x => ???),
      makeHttpFromRoute(teacher.TeacherConfirmAnswer, x => ???),

      makeHttpFromRoute(admin.AddCourseToGroup, x => ???),
      makeHttpFromRoute(admin.AddProblemToCourseTemplate, x => ???),
      makeHttpFromRoute(admin.AddUserToGroup, x => ???),
      makeHttpFromRoute(admin.AdminAction, x => ???),
      makeHttpFromRoute(admin.AdminCourseInfo, x => ???),
      makeHttpFromRoute(admin.CourseList, x => ???),
      makeHttpFromRoute(admin.GroupInfo, x => ???),
      makeHttpFromRoute(admin.GroupList, x => ???),
      makeHttpFromRoute(admin.NewCourseTemplate, x => ???),
      makeHttpFromRoute(admin.NewGroup, x => ???),
      makeHttpFromRoute(admin.ProblemTemplateList, x => ???),
      makeHttpFromRoute(admin.RemoveCustomProblemTemplate, x => ???),
      makeHttpFromRoute(admin.RemoveProblemFromCourseTemplate, x => ???),
      makeHttpFromRoute(admin.RemoveUserFromGroup, x => ???),
      makeHttpFromRoute(admin.UpdateCustomCourse, x => ???),
      makeHttpFromRoute(admin.UpdateCustomProblemTemplate, x => ???),
      makeHttpFromRoute(admin.UserList, x => ???),
    )

  val app2 = Handler.text("LOL KEK").toHttp
//  val app2 = Handler.text("LOL KEK").toHttp

  override def run = {
    Server.serve(app2).provide(Server.defaultWithPort(8080))
  }
}
