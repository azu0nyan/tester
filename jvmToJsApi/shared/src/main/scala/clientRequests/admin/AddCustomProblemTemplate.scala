package clientRequests.admin

import clientRequests.{Route, WithToken}
import AddCustomProblemTemplate.*

object AddCustomProblemTemplateJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[AddCustomProblemTemplateRequest] = deriveDecoder[AddCustomProblemTemplateRequest]
  implicit val reqEnc: Encoder[AddCustomProblemTemplateRequest] = deriveEncoder[AddCustomProblemTemplateRequest]
  implicit val resDec: Decoder[AddCustomProblemTemplateResponse] = deriveDecoder[AddCustomProblemTemplateResponse]
  implicit val resEnc: Encoder[AddCustomProblemTemplateResponse] = deriveEncoder[AddCustomProblemTemplateResponse]

}
import AddCustomProblemTemplateJson.*
object AddCustomProblemTemplate extends Route[AddCustomProblemTemplateRequest, AddCustomProblemTemplateResponse] {
  override val route: String = "requestAddCustomProblemTemplate"


}



//REQ
case class AddCustomProblemTemplateRequest(token: String, problemAlias: String, title: String, html: String) extends WithToken

//RES
sealed trait AddCustomProblemTemplateResponse
case class AddCustomProblemTemplateSuccess() extends AddCustomProblemTemplateResponse

sealed trait AddCustomProblemTemplateFailure extends AddCustomProblemTemplateResponse
case class UnknownAddCustomProblemTemplateFailure() extends AddCustomProblemTemplateFailure
case class AliasClaimed() extends AddCustomProblemTemplateFailure


