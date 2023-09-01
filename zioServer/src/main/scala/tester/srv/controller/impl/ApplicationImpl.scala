package tester.srv.controller.impl

import clientRequests.{CourseDataRequest, CourseDataResponse, CoursesListRequest, CoursesListResponse, GetCoursesListSuccess, LoginFailureUserNotFoundResponse, LoginFailureWrongPasswordResponse, LoginRequest, LoginResponse, LoginSuccessResponse, PartialCourseDataRequest, PartialCourseDataResponse, PartialCourseDataSuccess, ProblemDataRequest, ProblemDataResponse, ProblemDataSuccess, RegistrationRequest, RegistrationResponse, RequestStartCourseSuccess, StartCourseRequest, StartCourseResponse, SubmitAnswerRequest, SubmitAnswerResponse, UpdateUserDataRequest, UpdateUserDataResponse, UserDataRequest, UserDataResponse}
import clientRequests.admin.{AddCourseToGroupRequest, AddCourseToGroupResponse, AddCustomProblemTemplateRequest, AddCustomProblemTemplateResponse, AddProblemToCourseTemplateRequest, AddProblemToCourseTemplateResponse, AddUserToGroupFailure, AddUserToGroupRequest, AddUserToGroupResponse, AddUserToGroupSuccess, AdminActionRequest, AdminActionResponse, AdminCourseInfoRequest, AdminCourseInfoResponse, AdminCourseListRequest, AdminCourseListResponse, GroupInfoRequest, GroupInfoResponse, GroupListRequest, GroupListResponse, NewCourseTemplateRequest, NewCourseTemplateResponse, NewGroupRequest, NewGroupResponse, ProblemTemplateListRequest, ProblemTemplateListResponse, RemoveCustomProblemTemplateRequest, RemoveCustomProblemTemplateResponse, RemoveProblemFromCourseTemplateRequest, RemoveProblemFromCourseTemplateResponse, RemoveUserFromGroupRequest, RemoveUserFromGroupResponse, UpdateCustomCourseRequest, UpdateCustomCourseResponse, UpdateCustomProblemTemplateRequest, UpdateCustomProblemTemplateResponse, UserListRequest, UserListResponse}
import clientRequests.teacher.{AnswerForConfirmationListRequest, AnswerForConfirmationListResponse, AnswerForConfirmationListSuccess, AnswersListRequest, AnswersListResponse, AnswersListSuccess, CourseAnswersConfirmationInfo, ModifyProblemRequest, ModifyProblemResponse, ModifyProblemSuccess, RejectAnswer, SetScore, ShortCourseInfo, TeacherConfirmAnswerRequest, TeacherConfirmAnswerResponse, TeacherConfirmAnswerSuccess, UserConfirmationInfo}
import clientRequests.watcher.{GroupScoresRequest, GroupScoresResponse, LightGroupScoresRequest, LightGroupScoresResponse}
import io.github.gaelrenoux.tranzactio.doobie.{Database, TranzactIO}
import tester.srv.controller.AnswerService.AnswerFilterParams
import tester.srv.controller.UserService.{LoginResult, RegistrationResult}
import tester.srv.controller.{AnswerService, Application, CoursesService, GroupService, ProblemService, UserService}
import tester.srv.dao.AnswerDao
import viewData.AnswerViewData
import zio.*

case class ApplicationImpl(
                            db: Database,
                            answers: AnswerService,
                            groups: GroupService,
                            problems: ProblemService,
                            courses: CoursesService,
                            users: UserService
                          ) extends Application {
  override def answerForConfirmationList(req: AnswerForConfirmationListRequest): Task[AnswerForConfirmationListResponse] =
    db.transactionOrWiden(
      answers.unconfirmedAnswers(AnswerFilterParams(groupId = req.groupId, teacherId = req.teacherId))
    ).map(list =>
      list.groupBy(_._2.userId).toSeq.map((userId, answs) => UserConfirmationInfo(userId.toString,
        answs.groupBy(_._2.courseAlias).toSeq.map((alias, answs) => CourseAnswersConfirmationInfo(
          ShortCourseInfo(answs.headOption.map(_._2.courseId.toString).getOrElse(""), alias, answs.map(_._2.problemId.toString).distinct),
          answs.map(_.toViewData)
        ))
      ))
    ).map(AnswerForConfirmationListSuccess.apply)

  override def answersList(req: AnswersListRequest): Task[AnswersListResponse] =
    db.transactionOrWiden(
      answers.filterAnswers(AnswerService.filterSeqToParams(req.filters))
    ).map(list => AnswersListSuccess(list.map(_.toViewData)))

  override def modifyProblem(req: ModifyProblemRequest): Task[ModifyProblemResponse] =
    req.modifyType match
      case RejectAnswer(answerId, answerMessage, invalidateBy) =>
        db.transactionOrWiden(answers.rejectAnswer(answerId.toInt, answerMessage, invalidateBy.map(_.toInt)))
          .map(_ => ModifyProblemSuccess())
      case SetScore(problemId, problemScore) =>
        db.transactionOrWiden(problems.setScore(problemId.toInt, problemScore))
          .map(_ => ModifyProblemSuccess())

  override def teacherConfirmAnswer(req: TeacherConfirmAnswerRequest): Task[TeacherConfirmAnswerResponse] =
    db.transactionOrWiden {
      for {
        _ <- answers.confirmAnswer(req.answerId.toInt, Some(req.confirmedBy.toInt))
        _ <- problems.reportAnswerConfirmed(req.problemId.toInt, req.answerId.toInt, req.score)
        _ <- ZIO.when(req.review.nonEmpty)(answers.reviewAnswer(req.answerId.toInt, req.confirmedBy.toInt, req.review.get))
      } yield TeacherConfirmAnswerSuccess()
    }

  override def courseData(req: CourseDataRequest): Task[CourseDataResponse] =
    db.transactionOrWiden(courses.courseViewData(req.courseId.toInt)).map(cd => CourseDataSuccess(cd))

  override def coursesList(req: CoursesListRequest): Task[CoursesListResponse] =
    db.transactionOrWiden(courses.userCourses(req.userId)).map(c => GetCoursesListSuccess(c))

  override def login(req: LoginRequest): Task[LoginResponse] = //todo add metadata logging
    db.transactionOrWiden(users.loginUser(UserService.LoginData(req.login, req.password))).flatMap {
      case LoginResult.LoggedIn(token) =>
        for{
          u <- users.byLogin(req.login).map(_.get)
        } yield LoginSuccessResponse(token, u)
      case LoginResult.UserNotFound(login) => ZIO.succeed(LoginFailureUserNotFoundResponse())
      case LoginResult.WrongPassword(login, password) => ZIO.succeed(LoginFailureWrongPasswordResponse())
    }

  override def partialCourseData(req: PartialCourseDataRequest): Task[PartialCourseDataResponse] =
    db.transactionOrWiden(courses.partialCourseViewData(req.courseId.toInt)).map(pcd => PartialCourseDataSuccess(pcd))

  override def problemData(req: ProblemDataRequest): Task[ProblemDataResponse] =
    db.transactionOrWiden(problems.getViewData(req.problemId.toInt)).map(pd => ProblemDataSuccess(pd))

  override def registration(req: RegistrationRequest): Task[RegistrationResponse] =
    db.transactionOrWiden(users.registerUser(UserService.RegistrationData(req.login, req.password, req.firstName, req.lastName, req.email))).map {
      case RegistrationResult.Success(userId) => RegistrationSuccess()
      case RegistrationResult.AlreadyExists(login) => RegistrationFailureUserAlreadyExistsResponse()
      case RegistrationResult.LoginToShort(min) => RegistrationFailureLoginToShortResponse(min)
      case RegistrationResult.PasswordToShort(min) => RegistrationFailurePasswordToShortResponse(min)
      case RegistrationResult.WrongCharsInLogin => RegistrationFailureWrongCharsInLoginResponse()
      case RegistrationResult.ZeroRowsUpdated => RegistrationFailureUnknownErrorResponse()
      case RegistrationResult.UnknownError(t, msg) => RegistrationFailureUnknownErrorResponse()
    }

  override def startCourse(req: StartCourseRequest): Task[StartCourseResponse] =
    db.transactionOrWiden(courses.startCourseForUser(req.courseTemplateAlias, req.userId.toInt))
      .map(id => RequestStartCourseSuccess(id.toString))

  override def submitAnswer(req: SubmitAnswerRequest): Task[SubmitAnswerResponse] = ???
//    db.transactionOrWiden(answers.submitAnswer(req.problemIdHex.toInt, req.answerRaw)).flatMap {
//      case AnswerService.AnswerSubmitted(id) =>
//        answers.viewData(id)
//      case AnswerService.ProblemNotFound() => ???
//      case AnswerService.MaximumAttemptsLimitExceeded(attempts) => ???
//      case AnswerService.AlreadyVerifyingAnswer() => ???
//      case AnswerService.AnswerSubmissionClosed(cause) => ???
//    }

  override def updateUserData(req: UpdateUserDataRequest): Task[UpdateUserDataResponse] = ???
  override def userData(req: UserDataRequest): Task[UserDataResponse] = ???
  override def groupScores(req: GroupScoresRequest): Task[GroupScoresResponse] = ???
  override def lightGroupScores(req: LightGroupScoresRequest): Task[LightGroupScoresResponse] = ???

  override def addCourseToGroup(req: AddCourseToGroupRequest): Task[AddCourseToGroupResponse] = ???
  override def addCustomProblemTemplate(req: AddCustomProblemTemplateRequest): Task[AddCustomProblemTemplateResponse] = ???
  override def addProblemToCourseTemplate(req: AddProblemToCourseTemplateRequest): Task[AddProblemToCourseTemplateResponse] = ???
  override def addUserToGroup(req: AddUserToGroupRequest): Task[AddUserToGroupResponse] =
    db.transactionOrWiden(
        groups.addUserToGroup(req.UserHexIdOrLogin.toInt, req.groupIdOrTitle.toInt))
      .map {
        case true => AddUserToGroupSuccess()
        case false => AddUserToGroupFailure()
      }

  override def adminAction(req: AdminActionRequest): Task[AdminActionResponse] = ???
  override def adminCourseInfo(req: AdminCourseInfoRequest): Task[AdminCourseInfoResponse] = ???
  override def adminCourseList(req: AdminCourseListRequest): Task[AdminCourseListResponse] = ???
  override def groupInfo(req: GroupInfoRequest): Task[GroupInfoResponse] = ???
  override def groupList(req: GroupListRequest): Task[GroupListResponse] = ???
  override def newCourseTemplate(req: NewCourseTemplateRequest): Task[NewCourseTemplateResponse] = ???
  override def newGroup(req: NewGroupRequest): Task[NewGroupResponse] = ???
  override def problemTemplateList(req: ProblemTemplateListRequest): Task[ProblemTemplateListResponse] = ???
  override def removeCustomProblemTemplate(req: RemoveCustomProblemTemplateRequest): Task[RemoveCustomProblemTemplateResponse] = ???
  override def removeProblemFromCourseTemplate(req: RemoveProblemFromCourseTemplateRequest): Task[RemoveProblemFromCourseTemplateResponse] = ???
  override def removeUserFromGroup(req: RemoveUserFromGroupRequest): Task[RemoveUserFromGroupResponse] = ???
  override def updateCustomCourse(req: UpdateCustomCourseRequest): Task[UpdateCustomCourseResponse] = ???
  override def updateCustomProblemTemplate(req: UpdateCustomProblemTemplateRequest): Task[UpdateCustomProblemTemplateResponse] = ???
  override def userList(req: UserListRequest): Task[UserListResponse] = ???
}
