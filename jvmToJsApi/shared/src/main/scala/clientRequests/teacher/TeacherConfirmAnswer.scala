package clientRequests.teacher

import clientRequests.{Route, WithToken}
import otsbridge.ProblemScore.ProblemScore
import TeacherConfirmAnswer.*

object TeacherConfirmAnswerJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[TeacherConfirmAnswerRequest] = deriveDecoder[TeacherConfirmAnswerRequest]
  implicit val reqEnc: Encoder[TeacherConfirmAnswerRequest] = deriveEncoder[TeacherConfirmAnswerRequest]
  implicit val resDec: Decoder[TeacherConfirmAnswerResponse] = deriveDecoder[TeacherConfirmAnswerResponse]
  implicit val resEnc: Encoder[TeacherConfirmAnswerResponse] = deriveEncoder[TeacherConfirmAnswerResponse]

}
import TeacherConfirmAnswerJson.* 

object TeacherConfirmAnswer extends Route[TeacherConfirmAnswerRequest, TeacherConfirmAnswerResponse] {
  override val route: String = "requestTeacherConfirmAnswer"

}

//REQ
case class TeacherConfirmAnswerRequest(token:String, problemId: String, answerId:String, score:ProblemScore, review:Option[String], confirmedBy: String) extends WithToken

//RES
sealed trait TeacherConfirmAnswerResponse
case class TeacherConfirmAnswerSuccess() extends TeacherConfirmAnswerResponse

sealed trait TeacherConfirmAnswerFailure extends TeacherConfirmAnswerResponse
case class UnknownTeacherConfirmAnswerFailure() extends TeacherConfirmAnswerFailure

