package clientRequests

import SubmitAnswer.*
import viewData.AnswerViewData


object SubmitAnswerJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec12: Decoder[otsbridge.ProgramRunResult] = deriveDecoder[otsbridge.ProgramRunResult]
  implicit val resEnc12: Encoder[otsbridge.ProgramRunResult] = deriveEncoder[otsbridge.ProgramRunResult]


  implicit val reqDec: Decoder[SubmitAnswerRequest] = deriveDecoder[SubmitAnswerRequest]
  implicit val reqEnc: Encoder[SubmitAnswerRequest] = deriveEncoder[SubmitAnswerRequest]
  implicit val resDec: Decoder[SubmitAnswerResponse] = deriveDecoder[SubmitAnswerResponse]
  implicit val resEnc: Encoder[SubmitAnswerResponse] = deriveEncoder[SubmitAnswerResponse]

}

import SubmitAnswerJson.*

object SubmitAnswer extends Route[SubmitAnswerRequest , SubmitAnswerResponse] {
  override val route: String = "submitAnswer"
}

//REQ
case class SubmitAnswerRequest(token:String, problemIdHex:String, answerRaw:String) extends WithToken

//RES
sealed trait SubmitAnswerResponse
case class AnswerSubmitted(avd:AnswerViewData) extends SubmitAnswerResponse

case class ProblemNotFound() extends SubmitAnswerResponse
case class AlreadyVerifyingAnswer() extends SubmitAnswerResponse
case class MaximumAttemptsLimitExceeded(attempts:Int) extends SubmitAnswerResponse
case class AnswerSubmissionClosed(cause:Option[String]) extends SubmitAnswerResponse
case class RequestSubmitAnswerFailure(failure: GenericRequestFailure) extends SubmitAnswerResponse
case class UserCourseWithProblemNotFound() extends SubmitAnswerResponse
case class ProblemIsNotFromUserCourse() extends SubmitAnswerResponse