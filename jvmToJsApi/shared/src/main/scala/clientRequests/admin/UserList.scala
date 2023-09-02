package clientRequests.admin

import clientRequests.{Route, WithToken}
import viewData.UserViewData
import UserList.*

import java.time.Instant

object UserListJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[UserListRequest] = deriveDecoder[UserListRequest]
  implicit val reqEnc: Encoder[UserListRequest] = deriveEncoder[UserListRequest]
  implicit val resDec: Decoder[UserListResponse] = deriveDecoder[UserListResponse]
  implicit val resEnc: Encoder[UserListResponse] = deriveEncoder[UserListResponse]

  implicit val reqDec1: Decoder[clientRequests.admin.UserList.UserFilter] = deriveDecoder[clientRequests.admin.UserList.UserFilter]
  implicit val reqEnc1: Encoder[clientRequests.admin.UserList.UserFilter] = deriveEncoder[clientRequests.admin.UserList.UserFilter]

  implicit val reqDec2: Decoder[clientRequests.admin.UserList.UserOrder] = deriveDecoder[clientRequests.admin.UserList.UserOrder]
  implicit val reqEnc2: Encoder[clientRequests.admin.UserList.UserOrder] = deriveEncoder[clientRequests.admin.UserList.UserOrder]

}

import UserListJson.* 

object UserList extends Route[UserListRequest, UserListResponse] {
  override val route: String = "adminGetUserList"

  sealed trait UserOrder {
    def asc: Boolean
  }

  object UserOrder {
    case class ByDateRegistered(asc: Boolean) extends UserOrder
    case class ByLogin(asc: Boolean) extends UserOrder
    case class ByFirstName(asc: Boolean) extends UserOrder
    case class ByLastName(asc: Boolean) extends UserOrder
    case class ByEmail(asc: Boolean) extends UserOrder
  }

  sealed trait UserFilter
  object UserFilter {
    case class MatchesRegex(regex: String) extends UserFilter
    case object Teacher extends UserFilter
    case object User extends UserFilter
    case object Admin extends UserFilter
    case object Watcher extends UserFilter
  }
}

//REQ
case class UserListRequest(token:String, filters: Seq[UserFilter], itemsPerPage: Int = 200, page:Int = 0, order: Seq[UserOrder]) extends WithToken

//RES
sealed trait UserListResponse
case class UserListResponseSuccess(users:Seq[UserViewData]) extends UserListResponse
case class UserListResponseFailure() extends UserListResponse

