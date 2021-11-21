package clientRequests.watcher

import clientRequests.{GenericRequestFailure, Route, WithToken}
import io.circe.generic.auto._
import viewData.{UserGradeViewData, UserViewData}

import java.time.Instant


object GroupGrades extends Route[GroupGradesRequest, GroupGradesResponse] {
  override val route: String = "requestGroupGrades"
}

//REQ
case class GroupGradesRequest(token: String,
                              groupIdOrTitle: String,
                              onlyStudentGrades:Boolean,
                              loadPersonalGrades: Boolean = true,
                              from: Option[Instant] = None,
                              to: Option[Instant] = None
                             ) extends WithToken

//RES
sealed trait GroupGradesResponse
case class GroupGradesSuccess(grades: Seq[(UserViewData, Seq[UserGradeViewData])]) extends GroupGradesResponse
sealed trait GroupGradesFailure extends GroupGradesResponse
case class UnknownGroupGradesFailure() extends GroupGradesFailure

