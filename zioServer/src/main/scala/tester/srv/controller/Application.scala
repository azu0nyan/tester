package tester.srv.controller

import zio.*

import clientRequests.*
import clientRequests.admin.*
import clientRequests.teacher.*
import clientRequests.watcher.*

/** Binds old api to new one */
trait Application extends UserApp with TeacherApp with WatcherApp with AdminApp with BaseApp

trait BaseApp{
  def loadCourseTemplatesFromDb: Task[Unit]
  def loadProblemTemplatesFromDb: Task[Unit]
  def initCaches: Task[Unit]
} 

trait UserApp {
  def courseData(req: CourseDataRequest): Task[CourseDataResponse]
  def coursesList(req: CoursesListRequest): Task[CoursesListResponse]
  def login(req: LoginRequest): Task[LoginResponse]
  def partialCourseData(req: PartialCourseDataRequest): Task[PartialCourseDataResponse]
  def problemData(req: ProblemDataRequest): Task[ProblemDataResponse]
  def registration(req: RegistrationRequest): Task[RegistrationResponse]
  def startCourse(req: StartCourseRequest): Task[StartCourseResponse]
  def submitAnswer(req: SubmitAnswerRequest): Task[SubmitAnswerResponse]
  def updateUserData(req: UpdateUserDataRequest): Task[UpdateUserDataResponse]
  def userData(req: UserDataRequest): Task[UserDataResponse]
}

trait WatcherApp{
  def groupScores(req: GroupScoresRequest): Task[GroupScoresResponse]
  def lightGroupScores(req: LightGroupScoresRequest): Task[LightGroupScoresResponse]
}

trait TeacherApp{
  def answerForConfirmationList(req: AnswerForConfirmationListRequest): Task[AnswerForConfirmationListResponse]
  def answersList(req: AnswersListRequest): Task[AnswersListResponse]
  def modifyProblem(req: ModifyProblemRequest): Task[ModifyProblemResponse]
  def teacherConfirmAnswer(req: TeacherConfirmAnswerRequest): Task[TeacherConfirmAnswerResponse]
}

trait AdminApp{
  def addCourseToGroup(req: AddCourseToGroupRequest): Task[AddCourseToGroupResponse]
  def addCustomProblemTemplate(req: AddCustomProblemTemplateRequest): Task[AddCustomProblemTemplateResponse]
  def addProblemToCourseTemplate(req: AddProblemToCourseTemplateRequest): Task[AddProblemToCourseTemplateResponse]
  def addUserToGroup(req: AddUserToGroupRequest): Task[AddUserToGroupResponse]
  def adminAction(req: AdminActionRequest): Task[AdminActionResponse]
  def adminCourseInfo(req: AdminCourseInfoRequest): Task[AdminCourseInfoResponse]
  def adminCourseList(req: AdminCourseListRequest): Task[AdminCourseListResponse]
  def groupInfo(req: GroupInfoRequest): Task[GroupInfoResponse]
  def groupList(req: GroupListRequest): Task[GroupListResponse]
  def newCourseTemplate(req: NewCourseTemplateRequest): Task[NewCourseTemplateResponse]
  def newGroup(req: NewGroupRequest): Task[NewGroupResponse]
  def problemTemplateList(req: ProblemTemplateListRequest): Task[ProblemTemplateListResponse]
  def removeCustomProblemTemplate(req: RemoveCustomProblemTemplateRequest): Task[RemoveCustomProblemTemplateResponse]
  def removeProblemFromCourseTemplate(req: RemoveProblemFromCourseTemplateRequest): Task[RemoveProblemFromCourseTemplateResponse]
  def removeUserFromGroup(req: RemoveUserFromGroupRequest): Task[RemoveUserFromGroupResponse]
  def updateCustomCourse(req: UpdateCustomCourseRequest): Task[UpdateCustomCourseResponse]
  def updateCustomProblemTemplate(req: UpdateCustomProblemTemplateRequest): Task[UpdateCustomProblemTemplateResponse]
  def userList(req: UserListRequest): Task[UserListResponse]
}
