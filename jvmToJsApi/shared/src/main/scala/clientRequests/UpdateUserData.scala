package clientRequests

import clientRequests.{GenericRequestFailure, Route}
import UpdateUserData.* 

object UpdateUserDataJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[UpdateUserDataRequest] = deriveDecoder[UpdateUserDataRequest]
  implicit val reqEnc: Encoder[UpdateUserDataRequest] = deriveEncoder[UpdateUserDataRequest]
  implicit val resDec: Decoder[UpdateUserDataResponse] = deriveDecoder[UpdateUserDataResponse]
  implicit val resEnc: Encoder[UpdateUserDataResponse] = deriveEncoder[UpdateUserDataResponse]

}

import UpdateUserDataJson.*
object UpdateUserData extends Route[UpdateUserDataRequest, UpdateUserDataResponse] {
  override val route: String = "requestUpdateUserData"
}

//REQ
case class UpdateUserDataRequest(token: String, firstName: Option[String], lastName: Option[String], email:Option[String], oldPassword:Option[String], newPassword:Option[String]) extends WithToken

//RES
sealed trait UpdateUserDataResponse
case class UpdateUserDataSuccess() extends UpdateUserDataResponse
sealed trait UpdateUserDataFailure extends UpdateUserDataResponse
case class WrongPassword() extends UpdateUserDataFailure
case class UnknownUpdateUserDataFailure() extends UpdateUserDataFailure

