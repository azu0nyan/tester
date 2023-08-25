package clientRequests.watcher

import clientRequests.{GenericRequestFailure, Route, WithToken}
import viewData.{ProblemViewData, UserViewData}
import GroupScores.*

object GroupScoresJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[GroupScoresRequest] = deriveDecoder[GroupScoresRequest]
  implicit val reqEnc: Encoder[GroupScoresRequest] = deriveEncoder[GroupScoresRequest]
  implicit val resDec: Decoder[GroupScoresResponse] = deriveDecoder[GroupScoresResponse]
  implicit val resEnc: Encoder[GroupScoresResponse] = deriveEncoder[GroupScoresResponse]

  implicit val resDec1: Decoder[GroupCourseInfo] = deriveDecoder[GroupCourseInfo]
  implicit val resEnc1: Encoder[GroupCourseInfo] = deriveEncoder[GroupCourseInfo]

  implicit val resDec2: Decoder[GroupProblemInfo] = deriveDecoder[GroupProblemInfo]
  implicit val resEnc2: Encoder[GroupProblemInfo] = deriveEncoder[GroupProblemInfo]

}

import GroupScoresJson.* 

object GroupScores extends Route[GroupScoresRequest, GroupScoresResponse] {
  override val route: String = "requestGroupScores"

}

case class GroupProblemInfo(alias:String, title:String)

case class GroupCourseInfo(alias:String, title:String, problemTitleAlias:Seq[GroupProblemInfo])

//REQ
case class GroupScoresRequest(token:String, groupId:String, courseAliases:Seq[String], onlyStudentAnswers:Boolean = true) extends WithToken



//RES
sealed trait GroupScoresResponse
case class GroupScoresSuccess(courses:Seq[GroupCourseInfo], userAndProblems:Seq[(UserViewData, Seq[ProblemViewData])]) extends GroupScoresResponse
sealed trait GroupScoresFailure extends GroupScoresResponse
case class UnknownGroupScoresFailure() extends GroupScoresFailure

