package clientRequests

import Registration.* 


object RegistrationJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[RegistrationRequest] = deriveDecoder[RegistrationRequest]
  implicit val reqEnc: Encoder[RegistrationRequest] = deriveEncoder[RegistrationRequest]
  implicit val resDec: Decoder[RegistrationResponse] = deriveDecoder[RegistrationResponse]
  implicit val resEnc: Encoder[RegistrationResponse] = deriveEncoder[RegistrationResponse]

}

import RegistrationJson.*

object Registration extends Route[RegistrationRequest, RegistrationResponse] {
  override val route: String = "register"
}



case class RegistrationRequest(login:String, password:String, firstName:String, lastName:String, email: String)

sealed trait RegistrationResponse
sealed trait RegistrationFailure

case class RegistrationSuccess() extends RegistrationResponse
case class RegistrationFailureUserAlreadyExistsResponse() extends RegistrationResponse with RegistrationFailure
case class RegistrationFailureLoginToShortResponse(minLength:Int) extends RegistrationResponse with RegistrationFailure
case class RegistrationFailurePasswordToShortResponse(minLength:Int) extends RegistrationResponse with RegistrationFailure
case class RegistrationFailureWrongCharsInLoginResponse() extends RegistrationResponse with RegistrationFailure
case class RegistrationFailureUnknownErrorResponse() extends RegistrationResponse with RegistrationFailure
case class RegistrationFailureFrontendException(t:Throwable) extends RegistrationFailure //todo purge
