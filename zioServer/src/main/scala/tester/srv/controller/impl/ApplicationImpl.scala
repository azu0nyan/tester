package tester.srv.controller.impl

import clientRequests.{CourseDataRequest, CourseDataResponse, CoursesListRequest, CoursesListResponse, LoginRequest, LoginResponse, PartialCourseDataRequest, PartialCourseDataResponse, ProblemDataRequest, ProblemDataResponse, RegistrationRequest, RegistrationResponse, StartCourseRequest, StartCourseResponse, SubmitAnswerRequest, SubmitAnswerResponse, UpdateUserDataRequest, UpdateUserDataResponse, UserDataRequest, UserDataResponse}
import clientRequests.admin.{AddCourseToGroupRequest, AddCourseToGroupResponse, AddCustomProblemTemplateRequest, AddCustomProblemTemplateResponse, AddProblemToCourseTemplateRequest, AddProblemToCourseTemplateResponse, AddUserToGroupFailure, AddUserToGroupRequest, AddUserToGroupResponse, AddUserToGroupSuccess, AdminActionRequest, AdminActionResponse, AdminCourseInfoRequest, AdminCourseInfoResponse, AdminCourseListRequest, AdminCourseListResponse, GroupInfoRequest, GroupInfoResponse, GroupListRequest, GroupListResponse, NewCourseTemplateRequest, NewCourseTemplateResponse, NewGroupRequest, NewGroupResponse, ProblemTemplateListRequest, ProblemTemplateListResponse, RemoveCustomProblemTemplateRequest, RemoveCustomProblemTemplateResponse, RemoveProblemFromCourseTemplateRequest, RemoveProblemFromCourseTemplateResponse, RemoveUserFromGroupRequest, RemoveUserFromGroupResponse, UpdateCustomCourseRequest, UpdateCustomCourseResponse, UpdateCustomProblemTemplateRequest, UpdateCustomProblemTemplateResponse, UserListRequest, UserListResponse}
import clientRequests.teacher.{AnswerForConfirmationListRequest, AnswerForConfirmationListResponse, AnswerForConfirmationListSuccess, AnswersListRequest, AnswersListResponse, CourseAnswersConfirmationInfo, ModifyProblemRequest, ModifyProblemResponse, ShortCourseInfo, TeacherConfirmAnswerRequest, TeacherConfirmAnswerResponse, UserConfirmationInfo}
import clientRequests.watcher.{GroupScoresRequest, GroupScoresResponse, LightGroupScoresRequest, LightGroupScoresResponse}
import io.github.gaelrenoux.tranzactio.doobie.{Database, TranzactIO}
import tester.srv.controller.AnswerService.AnswerFilterParams
import tester.srv.controller.{AnswerService, Application}
import tester.srv.dao.AnswerDao
import viewData.AnswerViewData
import zio.*

case class ApplicationImpl(
                            db: Database,
                            answ: AnswerService[TranzactIO]
                          ) extends Application {
  override def answerForConfirmationList(req: AnswerForConfirmationListRequest): Task[AnswerForConfirmationListResponse] =
    db.transactionOrWiden(
      answ.unconfirmedAnswers(AnswerFilterParams(groupId = req.groupId, teacherId = req.teacherId))
    ).map(list =>
      list.groupBy(_._2.userId).toSeq.map((userId, answs) => UserConfirmationInfo(userId.toString,
        answs.groupBy(_._2.courseAlias).toSeq.map((alias, answs) => CourseAnswersConfirmationInfo(
          ShortCourseInfo(answs.headOption.map(_._2.courseId.toString).getOrElse(""), alias, answs.map(_._2.problemId.toString).distinct),
          answs.map( (a, meta, status) => AnswerViewData(a.id.toString, meta.problemId.toString, a.answer, a.answeredAt, status))
        ))
      ))
    ).map(AnswerForConfirmationListSuccess.apply)

  override def answersList(req: AnswersListRequest): Task[AnswersListResponse] = ???
  override def modifyProblem(req: ModifyProblemRequest): Task[ModifyProblemResponse] = ???
  override def teacherConfirmAnswer(req: TeacherConfirmAnswerRequest): Task[TeacherConfirmAnswerResponse] = ???
  override def courseData(req: CourseDataRequest): Task[CourseDataResponse] = ???
  override def coursesList(req: CoursesListRequest): Task[CoursesListResponse] = ???
  override def login(req: LoginRequest): Task[LoginResponse] = ???
  override def partialCourseData(req: PartialCourseDataRequest): Task[PartialCourseDataResponse] = ???
  override def problemData(req: ProblemDataRequest): Task[ProblemDataResponse] = ???
  override def registration(req: RegistrationRequest): Task[RegistrationResponse] = ???
  override def startCourse(req: StartCourseRequest): Task[StartCourseResponse] = ???
  override def submitAnswer(req: SubmitAnswerRequest): Task[SubmitAnswerResponse] = ???
  override def updateUserData(req: UpdateUserDataRequest): Task[UpdateUserDataResponse] = ???
  override def userData(req: UserDataRequest): Task[UserDataResponse] = ???
  override def groupScores(req: GroupScoresRequest): Task[GroupScoresResponse] = ???
  override def lightGroupScores(req: LightGroupScoresRequest): Task[LightGroupScoresResponse] = ???
  override def addCourseToGroup(req: AddCourseToGroupRequest): Task[AddCourseToGroupResponse] = ???
  override def addCustomProblemTemplate(req: AddCustomProblemTemplateRequest): Task[AddCustomProblemTemplateResponse] = ???
  override def addProblemToCourseTemplate(req: AddProblemToCourseTemplateRequest): Task[AddProblemToCourseTemplateResponse] = ???
  override def addUserToGroup(req: AddUserToGroupRequest): Task[AddUserToGroupResponse] =
    db.transactionOrWiden(
        GroupServiceTranzactIO.addUserToGroup(req.UserHexIdOrLogin.toInt, req.groupIdOrTitle.toInt))
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
