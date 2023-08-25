package clientRequests

import clientRequests.{GenericRequestFailure, Route}
import UserData.*
import viewData.UserViewData

object UserDataJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[UserDataRequest] = deriveDecoder[UserDataRequest]
  implicit val reqEnc: Encoder[UserDataRequest] = deriveEncoder[UserDataRequest]
  implicit val resDec: Decoder[UserDataResponse] = deriveDecoder[UserDataResponse]
  implicit val resEnc: Encoder[UserDataResponse] = deriveEncoder[UserDataResponse]

}

import UserDataJson.*

object UserData extends Route[UserDataRequest, UserDataResponse] {
  override val route: String = "requestGetUserData"
}

//REQ
case class UserDataRequest(token:String) extends WithToken

//RES
sealed trait UserDataResponse
case class UserDataSuccess(data: UserViewData) extends UserDataResponse
sealed trait UserDataFailure extends UserDataResponse
case class UnknownUserDataFailure() extends UserDataFailure

