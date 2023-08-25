package clientRequests.admin

import clientRequests.{GenericRequestFailure, Route, WithToken}
import viewData.ProblemTemplateExampleViewData
import ProblemTemplateList.*

object ProblemTemplateListJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec1: Decoder[ProblemTemplateFilter] = deriveDecoder[ProblemTemplateFilter]
  implicit val reqEnc1: Encoder[ProblemTemplateFilter] = deriveEncoder[ProblemTemplateFilter]

  implicit val reqDec: Decoder[ProblemTemplateListRequest] = deriveDecoder[ProblemTemplateListRequest]
  implicit val reqEnc: Encoder[ProblemTemplateListRequest] = deriveEncoder[ProblemTemplateListRequest]
  implicit val resDec: Decoder[ProblemTemplateListResponse] = deriveDecoder[ProblemTemplateListResponse]
  implicit val resEnc: Encoder[ProblemTemplateListResponse] = deriveEncoder[ProblemTemplateListResponse]

}
import ProblemTemplateListJson.* 

object ProblemTemplateList extends Route[ProblemTemplateListRequest, ProblemTemplateListResponse] {
  override val route: String = "requestProblemTemplateList"


}

sealed trait ProblemTemplateFilter
case class AliasOrTitleMatches(regex: String) extends ProblemTemplateFilter
case class Editable(editable: Boolean) extends ProblemTemplateFilter
//REQ
case class ProblemTemplateListRequest(token:String, filters: Seq[ProblemTemplateFilter]) extends WithToken

//RES
sealed trait ProblemTemplateListResponse
case class ProblemTemplateListSuccess(templates:Seq[ProblemTemplateExampleViewData]) extends ProblemTemplateListResponse
sealed trait ProblemTemplateListFailure extends ProblemTemplateListResponse
case class UnknownProblemTemplateListFailure() extends ProblemTemplateListFailure

