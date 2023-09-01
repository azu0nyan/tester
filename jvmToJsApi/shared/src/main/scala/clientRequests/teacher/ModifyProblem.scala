package clientRequests.teacher

import clientRequests.{GenericRequestFailure, Route, WithToken}
import otsbridge.ProblemScore.ProblemScore
import ModifyProblem.*

object ModifyProblemJson {

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[ModifyProblemRequest] = deriveDecoder[ModifyProblemRequest]
  implicit val reqEnc: Encoder[ModifyProblemRequest] = deriveEncoder[ModifyProblemRequest]
  implicit val resDec: Decoder[ModifyProblemResponse] = deriveDecoder[ModifyProblemResponse]
  implicit val resEnc: Encoder[ModifyProblemResponse] = deriveEncoder[ModifyProblemResponse]

}

import ModifyProblemJson.* 

object ModifyProblem extends Route[ModifyProblemRequest, ModifyProblemResponse] {
  override val route: String = "requestModifyProblem"
  
}

sealed trait ModifyType
case class RejectAnswer(answerId: String, answerMessage:Option[String], invalidateBy: Option[String]) extends ModifyType
case class SetScore(problemId: String, problemScore: ProblemScore) extends ModifyType
//REQ
case class ModifyProblemRequest(token:String, modifyType: ModifyType) extends WithToken

//RES
sealed trait ModifyProblemResponse
case class ModifyProblemSuccess() extends ModifyProblemResponse
sealed trait ModifyProblemFailure extends ModifyProblemResponse
case class UnknownModifyProblemFailure() extends ModifyProblemFailure

