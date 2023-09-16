package tester.srv.controller.impl

import clientRequests.{CourseDataRequest, CourseDataResponse, CourseDataSuccess, CourseNotFound, CoursesListRequest, CoursesListResponse, GetCoursesListSuccess, LoginFailureUserNotFoundResponse, LoginFailureWrongPasswordResponse, LoginRequest, LoginResponse, LoginSuccessResponse, PartialCourseDataRequest, PartialCourseDataResponse, PartialCourseDataSuccess, ProblemDataNotFound, ProblemDataRequest, ProblemDataResponse, ProblemDataSuccess, RegistrationFailureLoginToShortResponse, RegistrationFailurePasswordToShortResponse, RegistrationFailureUnknownErrorResponse, RegistrationFailureUserAlreadyExistsResponse, RegistrationFailureWrongCharsInLoginResponse, RegistrationRequest, RegistrationResponse, RegistrationSuccess, RequestStartCourseSuccess, StartCourseRequest, StartCourseResponse, SubmitAnswerRequest, SubmitAnswerResponse, UpdateUserDataRequest, UpdateUserDataResponse, UserDataRequest, UserDataResponse, UserDataSuccess}
import clientRequests.admin.{AddCourseToGroupRequest, AddCourseToGroupResponse, AddCourseToGroupSuccess, AddCustomProblemTemplateRequest, AddCustomProblemTemplateResponse, AddProblemToCourseTemplateRequest, AddProblemToCourseTemplateResponse, AddProblemToCourseTemplateSuccess, AddProblemToCourseTemplateUnknownFailure, AddUserToGroupFailure, AddUserToGroupRequest, AddUserToGroupResponse, AddUserToGroupSuccess, AdminActionRequest, AdminActionResponse, AdminCourseInfoRequest, AdminCourseInfoResponse, AdminCourseListRequest, AdminCourseListResponse, GroupInfoRequest, GroupInfoResponse, GroupInfoResponseSuccess, GroupListRequest, GroupListResponse, GroupListResponseSuccess, NewCourseTemplateRequest, NewCourseTemplateResponse, NewCourseTemplateSuccess, NewCourseTemplateUnknownFailure, NewGroupRequest, NewGroupResponse, NewGroupSuccess, ProblemTemplateListRequest, ProblemTemplateListResponse, RemoveCustomProblemTemplateRequest, RemoveCustomProblemTemplateResponse, RemoveProblemFromCourseTemplateRequest, RemoveProblemFromCourseTemplateResponse, RemoveProblemFromCourseTemplateSuccess, RemoveProblemFromCourseTemplateUnknownFailure, RemoveUserFromGroupFailure, RemoveUserFromGroupRequest, RemoveUserFromGroupResponse, RemoveUserFromGroupSuccess, UnknownAddCourseToGroupFailure, UnknownTemplateCourseToRemoveFrom, UnknownUpdateCustomCourseFailure, UpdateCustomCourseRequest, UpdateCustomCourseResponse, UpdateCustomCourseSuccess, UpdateCustomProblemTemplateRequest, UpdateCustomProblemTemplateResponse, UserListRequest, UserListResponse, UserListResponseSuccess}
import clientRequests.teacher.{AnswerForConfirmationListRequest, AnswerForConfirmationListResponse, AnswerForConfirmationListSuccess, AnswersListRequest, AnswersListResponse, AnswersListSuccess, CourseAnswersConfirmationInfo, ModifyProblemRequest, ModifyProblemResponse, ModifyProblemSuccess, RejectAnswer, SetScore, ShortCourseInfo, TeacherConfirmAnswerRequest, TeacherConfirmAnswerResponse, TeacherConfirmAnswerSuccess, UserConfirmationInfo}
import clientRequests.watcher.{GroupScoresRequest, GroupScoresResponse, LightGroupScoresRequest, LightGroupScoresResponse}
import io.github.gaelrenoux.tranzactio.doobie.{Database, TranzactIO}
import otsbridge.AnswerVerificationResult
import otsbridge.CourseTemplate.CourseTemplateData
import tester.srv.controller.AnswerService.{AnswerFilterParams, AnswerSubmitted}
import tester.srv.controller.UserService.{LoginResult, RegistrationResult}
import tester.srv.controller.{AdminService, AnswerService, AnswerVerificatorRegistry, Application, CourseTemplateRegistry, CourseTemplateService, CoursesService, GroupService, MessageBus, ProblemInfoRegistry, ProblemService, TeacherService, UserService, VerificationService}
import tester.srv.dao.{AnswerDao, DbCourseTemplateDao, DbProblemTemplateDao}
import tester.srv.dao.AnswerVerificationDao.AnswerVerification
import tester.srv.dao.DbProblemTemplateDao.DbProblemTemplate
import viewData.AnswerViewData
import zio.*

case class ApplicationImpl(
                            db: Database,
                            answers: AnswerService,
                            groups: GroupService,
                            problems: ProblemService,
                            courses: CoursesService,
                            courseTemplates: CourseTemplateService,
                            users: UserService,
                            verification: VerificationService,
                            teachers: TeacherService,
                            admins: AdminService
                          ) extends Application {
  override def answerForConfirmationList(req: AnswerForConfirmationListRequest): Task[AnswerForConfirmationListResponse] =
    db.transactionOrWiden(
      answers.unconfirmedAnswers(AnswerFilterParams(groupId = req.groupId.map(_.toInt), teacherId = req.teacherId.map(_.toInt)))
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
    db.transactionOrWiden(courses.courseViewData(req.courseId.toInt))
      .map{
        case Some(cd) => CourseDataSuccess(cd)
        case None => CourseNotFound()
      }

  override def coursesList(req: CoursesListRequest): Task[CoursesListResponse] =
    db.transactionOrWiden(courses.userCourses(req.userId.toInt)).map(c => GetCoursesListSuccess(viewData.UserCoursesInfoViewData(Seq(), c.map(_.toInfo))))

  override def login(req: LoginRequest): Task[LoginResponse] = //todo add metadata logging
    db.transactionOrWiden(for {
      logRes <- users.loginUser(UserService.LoginData(req.login, req.password))
      res <- logRes match
        case LoginResult.LoggedIn(token) =>
          (for {
            u <- users.byLogin(req.login).map(_.get)
          } yield LoginSuccessResponse(token, u, true, true)).mapError(db => db.fillInStackTrace()) //todo correct roles
        case LoginResult.UserNotFound(login) => ZIO.succeed(LoginFailureUserNotFoundResponse())
        case LoginResult.WrongPassword(login, password) => ZIO.succeed(LoginFailureWrongPasswordResponse())
    } yield res)

  override def partialCourseData(req: PartialCourseDataRequest): Task[PartialCourseDataResponse] =
    db.transactionOrWiden(courses.partialCourseViewData(req.courseId.toInt)).map(pcd => PartialCourseDataSuccess(pcd))

  override def problemData(req: ProblemDataRequest): Task[ProblemDataResponse] =
    db.transactionOrWiden(problems.getViewData(req.problemId.toInt))
      .map{
        case Some(pd) => ProblemDataSuccess(pd)
        case None => ProblemDataNotFound()
      }

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

  override def submitAnswer(req: SubmitAnswerRequest): Task[SubmitAnswerResponse] =
    db.transactionOrWiden(
      for {
        subRes <- answers.submitAnswer(req.problemIdHex.toInt, req.answerRaw)
        res <- subRes match
          case AnswerService.AnswerSubmitted(id) =>
            answers.byId(id).map(avd => clientRequests.AnswerSubmitted(avd.toViewData))
          case AnswerService.ProblemNotFound() =>
            ZIO.succeed(clientRequests.ProblemNotFound())
          case AnswerService.MaximumAttemptsLimitExceeded(attempts) =>
            ZIO.succeed(clientRequests.MaximumAttemptsLimitExceeded(attempts))
          case AnswerService.AlreadyVerifyingAnswer() =>
            ZIO.succeed(clientRequests.AlreadyVerifyingAnswer())
          case AnswerService.AnswerSubmissionClosed(cause) =>
            ZIO.succeed(clientRequests.AnswerSubmissionClosed(cause))
      } yield res)


  override def updateUserData(req: UpdateUserDataRequest): Task[UpdateUserDataResponse] = ???

  override def userData(req: UserDataRequest): Task[UserDataResponse] =
    db.transactionOrWiden(users.byId(req.id.toInt)).map(uvd => UserDataSuccess(uvd))

  @deprecated override def groupScores(req: GroupScoresRequest): Task[GroupScoresResponse] = ???

  override def lightGroupScores(req: LightGroupScoresRequest): Task[LightGroupScoresResponse] =
    db.transactionOrWiden(groups.groupScores(req.groupId.toInt, req.courseAliases, req.userIds.map(_.toInt)))
      .map(s => clientRequests.watcher.LightGroupScoresSuccess(s.values.flatMap(_.values).flatMap(s => s.map((a, s) => (a, a

        /** get problem title */
      ))).toMap, s)) //todo

  override def addCourseToGroup(req: AddCourseToGroupRequest): Task[AddCourseToGroupResponse] =
    db.transactionOrWiden(groups.addCourseTemplateToGroup(req.courseAlias, req.groupId.toInt, req.forceToGroupMembers))
      .map {
        case true => AddCourseToGroupSuccess()
        case false => UnknownAddCourseToGroupFailure() //todo change api
      }

  override def addCustomProblemTemplate(req: AddCustomProblemTemplateRequest): Task[AddCustomProblemTemplateResponse] = ???

  override def addProblemToCourseTemplate(req: AddProblemToCourseTemplateRequest): Task[AddProblemToCourseTemplateResponse] =
    db.transactionOrWiden(courseTemplates.addProblemToTemplateAndUpdateCourses(req.courseAlias, req.problemAlias))
      .map {
        case true => AddProblemToCourseTemplateSuccess()
        case false => AddProblemToCourseTemplateUnknownFailure(clientRequests.UnknownException()) //todo change api
      }

  override def addUserToGroup(req: AddUserToGroupRequest): Task[AddUserToGroupResponse] =
    db.transactionOrWiden(
        groups.addUserToGroup(req.UserHexIdOrLogin.toInt, req.groupIdOrTitle.toInt))
      .map {
        case true => AddUserToGroupSuccess()
        case false => AddUserToGroupFailure()
      }

  override def adminAction(req: AdminActionRequest): Task[AdminActionResponse] = ???
  @deprecated override def adminCourseInfo(req: AdminCourseInfoRequest): Task[AdminCourseInfoResponse] = ???
  @deprecated override def adminCourseList(req: AdminCourseListRequest): Task[AdminCourseListResponse] = ???

  override def groupInfo(req: GroupInfoRequest): Task[GroupInfoResponse] =
    db.transactionOrWiden(groups.groupDetailedInfo(req.groupId.toInt)).map(vd => GroupInfoResponseSuccess(vd))

  override def groupList(req: GroupListRequest): Task[GroupListResponse] =
    db.transactionOrWiden(groups.groupList()).map(list => GroupListResponseSuccess(list))

  override def newCourseTemplate(req: NewCourseTemplateRequest): Task[NewCourseTemplateResponse] =
    db.transactionOrWiden(courseTemplates.createNewTemplate(req.uniqueAlias, "")).map {
      case true => NewCourseTemplateSuccess()
      case false => NewCourseTemplateUnknownFailure() //todo check for AliasNotUnique
    }
  override def newGroup(req: NewGroupRequest): Task[NewGroupResponse] =
    db.transactionOrWiden(groups.newGroup(req.title, "")).map(id => NewGroupSuccess(id.toString))

  override def problemTemplateList(req: ProblemTemplateListRequest): Task[ProblemTemplateListResponse] = ???
  override def removeCustomProblemTemplate(req: RemoveCustomProblemTemplateRequest): Task[RemoveCustomProblemTemplateResponse] = ???

  override def removeProblemFromCourseTemplate(req: RemoveProblemFromCourseTemplateRequest): Task[RemoveProblemFromCourseTemplateResponse] =
    db.transactionOrWiden(courseTemplates.removeProblemFromTemplateAndUpdateCourses(req.courseAlias, req.problemAlias))
      .map {
        case true => RemoveProblemFromCourseTemplateSuccess()
        case false => UnknownTemplateCourseToRemoveFrom() //todo change api
      }

  override def removeUserFromGroup(req: RemoveUserFromGroupRequest): Task[RemoveUserFromGroupResponse] =
    db.transactionOrWiden(groups.removeUserFromGroup(req.userId.toInt, req.groupId.toInt))
      .map {
        case true => RemoveUserFromGroupSuccess()
        case false => RemoveUserFromGroupFailure()
      }

  override def updateCustomCourse(req: UpdateCustomCourseRequest): Task[UpdateCustomCourseResponse] =
    db.transactionOrWiden(courseTemplates.updateCourse(req.courseAlias, req.updatedData.description, req.updatedData.courseData))
      .map {
        case true => UpdateCustomCourseSuccess()
        case false => UnknownUpdateCustomCourseFailure()
      }

  override def updateCustomProblemTemplate(req: UpdateCustomProblemTemplateRequest): Task[UpdateCustomProblemTemplateResponse] = ???

  override def userList(req: UserListRequest): Task[UserListResponse] =
    db.transactionOrWiden(users.byFilterInOrder(req.filters, req.order, req.itemsPerPage, req.page))
      .map(l => UserListResponseSuccess(l.map(_.toViewData)))


  override def loadCourseTemplatesFromDb: Task[Unit] =
    db.transactionOrWiden(
      for{
        courses <- DbCourseTemplateDao.all
        _ <- ZIO.foreach(courses)(c => courseTemplates.registerTemplate(DbCourseTemplateDao.toCourseTemplate(c)))
      } yield ()
    )
  override def loadProblemTemplatesFromDb: Task[Unit]  =
    db.transactionOrWiden(
      for {
        ps <- DbProblemTemplateDao.all
        _ <- ZIO.foreach(ps)(p => problems.registerInfo(DbProblemTemplateDao.toProblemInfo(p)))
      } yield ()
    )


  def initCaches: Task[Unit] =
    db.transactionOrWiden(
      for{
        _ <- groups.initCaches
        _ <- teachers.initCaches
        _ <- admins.initCaches
      } yield()
    )
}

object ApplicationImpl {

  type Registies = CourseTemplateRegistry & ProblemInfoRegistry & AnswerVerificatorRegistry

  type AppServices = AnswerService & GroupService & ProblemService & CoursesService & CourseTemplateService & UserService & VerificationService &
    TeacherService & AdminService

  type AppContext = Database & AppServices

  def constructAppServices: ZLayer[Database & Registies, Nothing, AppServices] = {
    val bus = MessageBus.layer
    val problem = bus >>> ProblemServiceImpl.layer
    val courseTemplate = (bus ++ problem) >>> CourseTemplateServiceImpl.layer
    val verification = (bus ++ problem) >>> VerificationServiceImpl.layer
    val answer = (bus ++ verification) >>> AnswerServiceImpl.layer
    val courses = (bus ++ problem) >>> CoursesServiceImpl.layer
    val users = bus >>> UserServiceImpl.layer
    val groups = (bus ++ courses ++ users) >>> GroupServiceImpl.layer
    val teacher = bus >>> TeacherServiceImpl.layer
    val admin = bus >>> AdminServiceImpl.layer
    answer ++ groups ++ problem ++ courses ++ courseTemplate ++ users ++ verification ++ teacher ++ admin
  }

  def constructRegistries: ULayer[Registies] =
    CourseTemplateRegistryImpl.layer ++
      ProblemInfoRegistryImpl.layer ++
      AnswerVerificatorRegistryImpl.layer

  def layer: ZLayer[Database, Nothing, ApplicationImpl] = constructRegistries >>> constructAppServices >>> layerContext

  def liveContext: ZIO[AppContext, Nothing, ApplicationImpl] =
    for {
      db <- ZIO.service[Database]
      as <- ZIO.service[AnswerService]
      gs <- ZIO.service[GroupService]
      ps <- ZIO.service[ProblemService]
      cs <- ZIO.service[CoursesService]
      cts <- ZIO.service[CourseTemplateService]
      us <- ZIO.service[UserService]
      ver <- ZIO.service[VerificationService]
      te <- ZIO.service[TeacherService]
      ad <- ZIO.service[AdminService]
    } yield ApplicationImpl(db, as, gs, ps, cs, cts, us, ver, te, ad)

  def layerContext = ZLayer.fromZIO(liveContext)
}
