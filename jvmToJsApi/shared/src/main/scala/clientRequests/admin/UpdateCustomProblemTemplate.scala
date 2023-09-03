package clientRequests.admin

import clientRequests.{Route, WithToken}
import otsbridge.AnswerField
import otsbridge.ProblemScore.ProblemScore
import otsbridge.AnswerField._
import UpdateCustomProblemTemplate.*

object UpdateCustomProblemTemplateJson {

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*


  implicit val reqDec1: Decoder[otsbridge.ProgrammingLanguage] = deriveDecoder[otsbridge.ProgrammingLanguage]
  implicit val reqEnc1: Encoder[otsbridge.ProgrammingLanguage] = deriveEncoder[otsbridge.ProgrammingLanguage]

  implicit val reqDec2: Decoder[otsbridge.ProgramRunResult] = deriveDecoder[otsbridge.ProgramRunResult]
  implicit val reqEnc2: Encoder[otsbridge.ProgramRunResult] = deriveEncoder[otsbridge.ProgramRunResult]


  implicit val reqDec: Decoder[UpdateCustomProblemTemplateRequest] = deriveDecoder[UpdateCustomProblemTemplateRequest]
  implicit val reqEnc: Encoder[UpdateCustomProblemTemplateRequest] = deriveEncoder[UpdateCustomProblemTemplateRequest]
  implicit val resDec: Decoder[UpdateCustomProblemTemplateResponse] = deriveDecoder[UpdateCustomProblemTemplateResponse]
  implicit val resEnc: Encoder[UpdateCustomProblemTemplateResponse] = deriveEncoder[UpdateCustomProblemTemplateResponse]

}

import UpdateCustomProblemTemplateJson.*


object UpdateCustomProblemTemplate extends Route[UpdateCustomProblemTemplateRequest, UpdateCustomProblemTemplateResponse] {
  override val route: String = "requestUpdateCustomProblemTemplate"
  
 }


case class CustomProblemUpdateData(
                                 title: String,
                                 html: String,
                                 answerField: AnswerField,
                                 initialScore: ProblemScore,
                                 //todo verification: CustomProblemVerification here
                                 )

//REQ
case class UpdateCustomProblemTemplateRequest(token: String, problemAlias: String, data: CustomProblemUpdateData) extends WithToken

//RES
sealed trait UpdateCustomProblemTemplateResponse
case class UpdateCustomProblemTemplateSuccess() extends UpdateCustomProblemTemplateResponse

sealed trait UpdateCustomProblemTemplateFailure extends UpdateCustomProblemTemplateResponse
case class UnknownUpdateCustomProblemTemplateFailure() extends UpdateCustomProblemTemplateFailure



