package clientRequests.admin

import clientRequests.{Route, WithToken}
import RemoveCustomProblemTemplate.*


object RemoveCustomProblemTemplateJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[RemoveCustomProblemTemplateRequest] = deriveDecoder[RemoveCustomProblemTemplateRequest]
  implicit val reqEnc: Encoder[RemoveCustomProblemTemplateRequest] = deriveEncoder[RemoveCustomProblemTemplateRequest]
  implicit val resDec: Decoder[RemoveCustomProblemTemplateResponse] = deriveDecoder[RemoveCustomProblemTemplateResponse]
  implicit val resEnc: Encoder[RemoveCustomProblemTemplateResponse] = deriveEncoder[RemoveCustomProblemTemplateResponse]

}

import RemoveCustomProblemTemplateJson.* 

object RemoveCustomProblemTemplate extends Route[RemoveCustomProblemTemplateRequest, RemoveCustomProblemTemplateResponse] {
  override val route: String = "requestRemoveCustomProblemTemplate"
}



//REQ
case class RemoveCustomProblemTemplateRequest(token: String, problemAlias: String) extends WithToken

//RES
sealed trait RemoveCustomProblemTemplateResponse
case class RemoveCustomProblemTemplateSuccess() extends RemoveCustomProblemTemplateResponse

sealed trait RemoveCustomProblemTemplateFailure extends RemoveCustomProblemTemplateResponse
case class UnknownRemoveCustomProblemTemplateFailure() extends RemoveCustomProblemTemplateFailure


