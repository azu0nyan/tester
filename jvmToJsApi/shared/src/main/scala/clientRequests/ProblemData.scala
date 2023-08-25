package clientRequests

import clientRequests.{GenericRequestFailure, Route}
import viewData.ProblemViewData

import ProblemData.*
object ProblemDataJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[ProblemDataRequest] = deriveDecoder[ProblemDataRequest]
  implicit val reqEnc: Encoder[ProblemDataRequest] = deriveEncoder[ProblemDataRequest]
  implicit val resDec: Decoder[ProblemDataResponse] = deriveDecoder[ProblemDataResponse]
  implicit val resEnc: Encoder[ProblemDataResponse] = deriveEncoder[ProblemDataResponse]

}

import ProblemDataJson.* 

object ProblemData extends Route[ProblemDataRequest, ProblemDataResponse] {
  override val route: String = "requestGetProblemData"

}

//REQ
case class ProblemDataRequest(token:String, problemId:String) extends WithToken

//RES
sealed trait ProblemDataResponse
case class ProblemDataSuccess(problemViewData:ProblemViewData) extends ProblemDataResponse
sealed trait ProblemDataFailure extends ProblemDataResponse
case class UnknownProblemDataFailure() extends ProblemDataFailure

