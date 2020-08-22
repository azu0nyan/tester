package clientRequests.admin

class UserList {

}

import clientRequests.Route
import io.circe.generic.auto._
import viewData.UserViewData


object UserList extends Route[UserListRequest, UserListResponse] {
  override val route: String = "adminGetUserList"
}

sealed trait UserListFilter
case class FilterUserByGroup(groupHexIdOrAlias:String)

//REQ
case class UserListRequest(token:String, filters:Seq[UserListFilter])

//RES
sealed trait UserListResponse
case class UserListResponseSuccess(users:Seq[UserViewData]) extends UserListResponse
case class UserListResponseFailure() extends UserListResponse

