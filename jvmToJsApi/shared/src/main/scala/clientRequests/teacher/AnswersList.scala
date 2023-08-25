package clientRequests.teacher

import clientRequests.{ProblemDataRequest, ProblemDataResponse, Route, WithToken}
import viewData.AnswerFullViewData
import AnswersList.*


object AnswerListJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[AnswersListRequest] = deriveDecoder[AnswersListRequest]
  implicit val reqEnc: Encoder[AnswersListRequest] = deriveEncoder[AnswersListRequest]
  implicit val resDec: Decoder[AnswersListResponse] = deriveDecoder[AnswersListResponse]
  implicit val resEnc: Encoder[AnswersListResponse] = deriveEncoder[AnswersListResponse]

  implicit val reqDec2: Decoder[AnswersListFilter] = deriveDecoder[AnswersListFilter]
  implicit val reqEnc2: Encoder[AnswersListFilter] = deriveEncoder[AnswersListFilter]

}

import AnswerListJson.*

object AnswersList extends Route[AnswersListRequest, AnswersListResponse] {
  override val route: String = "requestAnswersList"
}

sealed trait AnswersListFilter
case class ByGroupId(id: String) extends AnswersListFilter
case class ByProblemTemplate(templateAlias: String) extends AnswersListFilter
case object AwaitingConfirmation extends AnswersListFilter
case class WithScoreGEqThan(x:Double) extends AnswersListFilter
case class WithScoreLessThan(x:Double) extends AnswersListFilter

//REQ
case class AnswersListRequest(token: String, filters: Seq[AnswersListFilter], orderByDateAsc: Boolean, limit: Option[Int]) extends WithToken{
  def toStringWOToken: String = toString.replace(token, "")
}

//RES
sealed trait AnswersListResponse
case class AnswersListSuccess(answers: Seq[AnswerFullViewData]) extends AnswersListResponse
sealed trait AnswersListFailure extends AnswersListResponse
case class UnknownAnswersListFailure() extends AnswersListFailure

