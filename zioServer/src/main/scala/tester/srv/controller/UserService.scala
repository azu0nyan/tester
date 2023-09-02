package tester.srv.controller

import clientRequests.admin.UserList.{UserFilter, UserOrder}
import doobie.util.transactor.Transactor
import tester.srv.controller.UserService.*
import tester.srv.controller.UserService.LoginResult.*
import tester.srv.controller.UserService.RegistrationResult.*

import java.time.Instant
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import tester.srv.dao.RegisteredUserDao.RegisteredUser
import zio.*

trait UserService {
  def registerUser(req: RegistrationData): TranzactIO[RegistrationResult]

  def loginUser(data: LoginData): TranzactIO[LoginResult]

  def validateToken(token: String): TranzactIO[TokenOps.ValidationResult]

  def byLogin(login: String): TranzactIO[Option[viewData.UserViewData]]

  def byId(id: Int): TranzactIO[viewData.UserViewData]

  def byFilterInOrder(filters: Seq[UserFilter], order: Seq[UserOrder],
                      itemsPerPage: Int, page: Int): TranzactIO[Seq[RegisteredUser]]
}


object UserService {
  def registerUser(req: RegistrationData): ZIO[Transactor[Task] & UserService, Throwable, RegistrationResult] =
    ZIO.serviceWithZIO[UserService](_.registerUser(req))

  def loginUser(data: LoginData): ZIO[Transactor[Task] & UserService, Throwable, LoginResult] =
    ZIO.serviceWithZIO[UserService](_.loginUser(data))

  def validateToken(token: String): ZIO[Transactor[Task] & UserService, Throwable, TokenOps.ValidationResult] =
    ZIO.serviceWithZIO[UserService](_.validateToken(token))

  def byLogin(login: String): ZIO[Transactor[Task] & UserService, Throwable, Option[viewData.UserViewData]] =
    ZIO.serviceWithZIO[UserService](_.byLogin(login))

  def byId(id: Int): ZIO[Transactor[Task] & UserService, Throwable, viewData.UserViewData] =
    ZIO.serviceWithZIO[UserService](_.byId(id))

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

