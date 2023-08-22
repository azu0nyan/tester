package tester.srv.controller

import tester.srv.controller.UserService.*
import tester.srv.controller.UserService.LoginResult.*
import tester.srv.controller.UserService.RegistrationResult.*

import java.time.Instant


trait UserService[F[_]] {
  def registerUser(req: RegistrationData): F[RegistrationResult]

  def loginUser(data: LoginData): F[LoginResult]

  def validateToken(token: String): F[TokenOps.ValidationResult]
}


object UserService {
  case class UserFromList(login: String, firstName: String, lastName: String)

  sealed trait RegistrationResult
  object RegistrationResult {
    case class Success(userId: Int) extends RegistrationResult
    sealed trait Fail extends RegistrationResult
    case class AlreadyExists(login: String) extends Fail
    case class LoginToShort(min: Int) extends Fail
    case class PasswordToShort(min: Int) extends Fail
    case object WrongCharsInLogin extends Fail
    case object ZeroRowsUpdated extends Fail
    case class UnknownError(t: Option[Throwable] = None, msg: Option[String] = None) extends Fail
  }
  case class RegistrationData(login: String, password: String, firstName: String, lastName: String, email: String)


  case class LoginData(login: String, password: String, sessionLengthSec: Int = 24 * 60 * 60,
                       ip: Option[String] = None, userAgent: Option[String] = None, platform: Option[String] = None, locale: Option[String] = None,
                      )

  sealed trait LoginResult
  object LoginResult {
    final case class LoggedIn(token: String) extends LoginResult
    final case class UserNotFound(login: String) extends LoginResult
    final case class WrongPassword(login: String, password: String) extends LoginResult
  }

}

