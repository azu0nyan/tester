package clientRequests

import viewData.UserViewData
import Login.*

object LoginJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[LoginRequest] = deriveDecoder[LoginRequest]
  implicit val reqEnc: Encoder[LoginRequest] = deriveEncoder[LoginRequest]
  implicit val resDec: Decoder[LoginResponse] = deriveDecoder[LoginResponse]
  implicit val resEnc: Encoder[LoginResponse] = deriveEncoder[LoginResponse]

}

import LoginJson.*

object Login extends Route[LoginRequest, LoginResponse] {
  override val route: String = "login"
}

case class LoginRequest(login:String, password:String)

sealed trait LoginResponse
sealed trait LoginFailure
case class LoginSuccessResponse(token:String, userData:UserViewData) extends LoginResponse
case class LoginFailureUserNotFoundResponse() extends LoginResponse with LoginFailure
case class LoginFailureWrongPasswordResponse() extends LoginResponse with LoginFailure
case class LoginFailureUnknownErrorResponse() extends LoginResponse with LoginFailure
case class LoginFailureFrontendException(t:Throwable) extends LoginFailure


