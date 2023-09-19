package tester.srv.controller.impl

import clientRequests.{CourseDataRequest, CourseDataResponse, CoursesListRequest, CoursesListResponse, LoginRequest, LoginResponse, PartialCourseDataRequest, PartialCourseDataResponse, ProblemDataRequest, ProblemDataResponse, RegistrationRequest, RegistrationResponse, StartCourseRequest, StartCourseResponse, SubmitAnswerRequest, SubmitAnswerResponse, UpdateUserDataRequest, UpdateUserDataResponse, UserDataRequest, UserDataResponse, WithToken}
import clientRequests.admin.{AddCourseToGroupRequest, AddCourseToGroupResponse, AddCustomProblemTemplateRequest, AddCustomProblemTemplateResponse, AddProblemToCourseTemplateRequest, AddProblemToCourseTemplateResponse, AddUserToGroupRequest, AddUserToGroupResponse, AdminActionRequest, AdminActionResponse, AdminCourseInfoRequest, AdminCourseInfoResponse, AdminCourseListRequest, AdminCourseListResponse, GroupInfoRequest, GroupInfoResponse, GroupListRequest, GroupListResponse, NewCourseTemplateRequest, NewCourseTemplateResponse, NewGroupRequest, NewGroupResponse, ProblemTemplateListRequest, ProblemTemplateListResponse, RemoveCustomProblemTemplateRequest, RemoveCustomProblemTemplateResponse, RemoveProblemFromCourseTemplateRequest, RemoveProblemFromCourseTemplateResponse, RemoveUserFromGroupRequest, RemoveUserFromGroupResponse, UpdateCustomCourseRequest, UpdateCustomCourseResponse, UpdateCustomProblemTemplateRequest, UpdateCustomProblemTemplateResponse, UserListRequest, UserListResponse}
import clientRequests.teacher.{AnswerForConfirmationListRequest, AnswerForConfirmationListResponse, AnswersListRequest, AnswersListResponse, ModifyProblemRequest, ModifyProblemResponse, TeacherConfirmAnswerRequest, TeacherConfirmAnswerResponse}
import clientRequests.watcher.{GroupScoresRequest, GroupScoresResponse, LightGroupScoresRequest, LightGroupScoresResponse}
import io.github.gaelrenoux.tranzactio.doobie.Database
import tester.srv.controller.{AdminService, AnswerService, Application, CourseTemplateService, CoursesService, GroupService, ProblemService, PublicApp, TeacherService, TokenOps, UserService, VerificationService}
import zio.*

case class SecureApplication(
                              db: Database,
                              app: Application,
                              groups: GroupService,
                              courses: CoursesService,
                              users: UserService,
                              teachers: TeacherService,
                              admins: AdminService
                            ) extends PublicApp {

  import SecureApplication.*
  import SecureApplication.AllowWithUser
  import SecureApplication.AllowWithUser.*

  implicit val appl: Application = app
  implicit val secure: SecureApplication = this

  //  override def loadCourseTemplatesFromDb: Task[Unit] = app.loadCourseTemplatesFromDb
  //  override def loadProblemTemplatesFromDb: Task[Unit] = app.loadProblemTemplatesFromDb
  //  override def initCaches: Task[Unit] = app.initCaches

  override def courseData(req: CourseDataRequest): Task[CourseDataResponse] =
    AllowOr(AllowTeacherAndAdmin, AllowCourseAccess(req.courseId.toInt))
      .runIfAllowed(req, app.courseData)

  override def coursesList(req: CoursesListRequest): Task[CoursesListResponse] =
    AllowOr(AllowTeacherAndAdmin, AllowSameId(req.userId.toInt))
      .runIfAllowed(req, app.coursesList)

  override def login(req: LoginRequest): Task[LoginResponse] =
    app.login(req)

  override def partialCourseData(req: PartialCourseDataRequest): Task[PartialCourseDataResponse] =
    AllowOr(AllowTeacherAndAdmin, AllowCourseAccess(req.courseId.toInt))
      .runIfAllowed(req, app.partialCourseData)

  override def problemData(req: ProblemDataRequest): Task[ProblemDataResponse] =
    AllowOr(AllowTeacherAndAdmin, AllowProblemAccess(req.problemId.toInt))
      .runIfAllowed(req, app.problemData)

  override def registration(req: RegistrationRequest): Task[RegistrationResponse] =
    app.registration(req)

  override def startCourse(req: StartCourseRequest): Task[StartCourseResponse] =
    AllowTeacherAndAdmin
      .runIfAllowed(req, app.startCourse)

  override def submitAnswer(req: SubmitAnswerRequest): Task[SubmitAnswerResponse] =
    AllowProblemAccess(req.problemIdHex.toInt)
      .runIfAllowed(req, app.submitAnswer)

  override def updateUserData(req: UpdateUserDataRequest): Task[UpdateUserDataResponse] =
    AllowUser
      .runIfAllowed(req, app.updateUserData)


  override def userData(req: UserDataRequest): Task[UserDataResponse] =
    AllowOr(AllowTeacherAndAdmin, AllowSameId(req.id.toInt)) //todo more granular access for teachers
      .runIfAllowed(req, app.userData)


  //todo more granular access for teachers
  override def answerForConfirmationList(req: AnswerForConfirmationListRequest): Task[AnswerForConfirmationListResponse] =
    AllowTeacherAndAdmin
      .runIfAllowed(req, app.answerForConfirmationList)

  override def answersList(req: AnswersListRequest): Task[AnswersListResponse] =
    AllowTeacherAndAdmin  //todo more granular access for teachers
      .runIfAllowed(req, app.answersList)

  override def modifyProblem(req: ModifyProblemRequest): Task[ModifyProblemResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.modifyProblem)

  override def teacherConfirmAnswer(req: TeacherConfirmAnswerRequest): Task[TeacherConfirmAnswerResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.teacherConfirmAnswer)

  override def addCourseToGroup(req: AddCourseToGroupRequest): Task[AddCourseToGroupResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.addCourseToGroup)

  override def addCustomProblemTemplate(req: AddCustomProblemTemplateRequest): Task[AddCustomProblemTemplateResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.addCustomProblemTemplate)

  override def addProblemToCourseTemplate(req: AddProblemToCourseTemplateRequest): Task[AddProblemToCourseTemplateResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.addProblemToCourseTemplate)

  override def addUserToGroup(req: AddUserToGroupRequest): Task[AddUserToGroupResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.addUserToGroup)

  override def adminAction(req: AdminActionRequest): Task[AdminActionResponse] =
    AllowAdmin
      .runIfAllowed(req, app.adminAction)

  override def adminCourseInfo(req: AdminCourseInfoRequest): Task[AdminCourseInfoResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.adminCourseInfo)

  override def adminCourseList(req: AdminCourseListRequest): Task[AdminCourseListResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.adminCourseList)

  override def groupInfo(req: GroupInfoRequest): Task[GroupInfoResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers and users
      .runIfAllowed(req, app.groupInfo)

  override def groupList(req: GroupListRequest): Task[GroupListResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.groupList)

  override def newCourseTemplate(req: NewCourseTemplateRequest): Task[NewCourseTemplateResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.newCourseTemplate)

  override def newGroup(req: NewGroupRequest): Task[NewGroupResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.newGroup)

  override def problemTemplateList(req: ProblemTemplateListRequest): Task[ProblemTemplateListResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.problemTemplateList)

  override def removeCustomProblemTemplate(req: RemoveCustomProblemTemplateRequest): Task[RemoveCustomProblemTemplateResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.removeCustomProblemTemplate)

  override def removeProblemFromCourseTemplate(req: RemoveProblemFromCourseTemplateRequest): Task[RemoveProblemFromCourseTemplateResponse] =
  AllowTeacherAndAdmin //todo more granular access for teachers
    .runIfAllowed(req, app.removeProblemFromCourseTemplate)

  override def removeUserFromGroup(req: RemoveUserFromGroupRequest): Task[RemoveUserFromGroupResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.removeUserFromGroup)

  override def updateCustomCourse(req: UpdateCustomCourseRequest): Task[UpdateCustomCourseResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.updateCustomCourse)

  override def updateCustomProblemTemplate(req: UpdateCustomProblemTemplateRequest): Task[UpdateCustomProblemTemplateResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.updateCustomProblemTemplate)

  override def userList(req: UserListRequest): Task[UserListResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.userList)

  override def groupScores(req: GroupScoresRequest): Task[GroupScoresResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.groupScores)

  override def lightGroupScores(req: LightGroupScoresRequest): Task[LightGroupScoresResponse] =
    AllowTeacherAndAdmin //todo more granular access for teachers
      .runIfAllowed(req, app.lightGroupScores)

  def extractUser(req: Any): UIO[Option[UserAbilities]] =
    req match
      case withToken: WithToken => TokenOps.decodeAndValidateUserToken(withToken.token) match
        case TokenOps.TokenValid(id) =>
          for {
            t <- teachers.isTeacher(id)
            a <- admins.isAdmin(id)
            ugs <- groups.userGroups(id)
            tgs <- teachers.teacherGroups(id)
          } yield Some(UserAbilities(id, t, a, ugs, tgs))
        case _ => ZIO.succeed(None)
      case _ => ZIO.succeed(None)

}

object SecureApplication {

  def layer: ZLayer[ApplicationImpl.AppContext & Application, Nothing, SecureApplication] = ZLayer.fromZIO(liveContext)

  def liveContext: ZIO[ApplicationImpl.AppContext & Application, Nothing, SecureApplication] =
    for {
      db <- ZIO.service[Database]
      app <- ZIO.service[Application]
      as <- ZIO.service[AnswerService]
      gs <- ZIO.service[GroupService]
      ps <- ZIO.service[ProblemService]
      cs <- ZIO.service[CoursesService]
      cts <- ZIO.service[CourseTemplateService]
      us <- ZIO.service[UserService]
      ver <- ZIO.service[VerificationService]
      te <- ZIO.service[TeacherService]
      ad <- ZIO.service[AdminService]
    } yield SecureApplication(db, app, gs, cs, us, te, ad)

  case class UserAbilities(id: Int, teacher: Boolean, admin: Boolean, userGroups: Set[Int], teacherGroups: Set[Int])

  trait Allow {
    def runIfAllowed[REQ, RES](req: REQ, func: REQ => Task[RES])(implicit secure: SecureApplication, app: Application): Task[RES]
  }

  trait AllowWithUser extends Allow {
    def allowForUser[REQ](req: REQ, userAbilities: UserAbilities)
                         (implicit secure: SecureApplication, app: Application): Task[Boolean]

    override def runIfAllowed[REQ, RES](req: REQ, func: REQ => Task[RES])
                                       (implicit secure: SecureApplication, app: Application): Task[RES] =
      for {
        u <- secure.extractUser(req)
        allow <- u match
          case Some(uab) => allowForUser(req, uab)
          case None => ZIO.fail(SecurityException())
        res <-
          if (allow) func(req)
          else ZIO.fail(SecurityException())
      } yield res
  }

  object AllowWithUser {   


    case class AllowOr(allows: AllowWithUser*) extends AllowWithUser {
      override def allowForUser[REQ](req: REQ, userAbilities: UserAbilities)
                                    (implicit secure: SecureApplication, app: Application): Task[Boolean] =
        ZIO.exists(allows)(allow => allow.allowForUser(req, userAbilities))
    }

    case class AllowAnd(allows: AllowWithUser*) extends AllowWithUser {
      override def allowForUser[REQ](req: REQ, userAbilities: UserAbilities)
                                    (implicit secure: SecureApplication, app: Application): Task[Boolean] =
        ZIO.forall(allows)(allow => allow.allowForUser(req, userAbilities))
    }
    ////////////
    ////////////
    val AllowTeacherAndAdmin: AllowWithUser = AllowOr(AllowTeacher, AllowAdmin)

    case object AllowUser extends AllowWithUser {
      override def allowForUser[REQ](req: REQ, userAbilities: UserAbilities)
                                    (implicit secure: SecureApplication, app: Application): Task[Boolean] =
        ZIO.succeed(true)
    }

    case object AllowTeacher extends AllowWithUser {
      override def allowForUser[REQ](req: REQ, userAbilities: UserAbilities)
                                    (implicit secure: SecureApplication, app: Application): Task[Boolean] =
        if (userAbilities.teacher) ZIO.succeed(true)
        else ZIO.fail(SecurityException(Some("Not a teacher")))
    }

    case object AllowAdmin extends AllowWithUser {
      override def allowForUser[REQ](req: REQ, userAbilities: UserAbilities)
                                    (implicit secure: SecureApplication, app: Application): Task[Boolean] =
        if (userAbilities.admin) ZIO.succeed(true)
        else ZIO.fail(SecurityException(Some("Not a admin")))
    }

    case class AllowCourseAccess(courseId: Int) extends AllowWithUser {
      override def allowForUser[REQ](req: REQ, userAbilities: UserAbilities)
                                    (implicit secure: SecureApplication, app: Application): Task[Boolean] =
        ZIO.succeed(true) //todo
    }

    case class AllowProblemAccess(problemId: Int) extends AllowWithUser {
      override def allowForUser[REQ](req: REQ, userAbilities: UserAbilities)
                                    (implicit secure: SecureApplication, app: Application): Task[Boolean] =
        ZIO.succeed(true) //todo
    }

    case class AllowSameId(userId: Int) extends AllowWithUser {
      override def allowForUser[REQ](req: REQ, userAbilities: UserAbilities)
                                    (implicit secure: SecureApplication, app: Application): Task[Boolean] =
        ZIO.succeed(userAbilities.id == userId)
    }

  }

  case class SecurityException(message: Option[String] = None) extends Exception(message.getOrElse(s"Security exception"))
}
