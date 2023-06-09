package clientRequests.admin


import clientRequests.{Route, WithToken}
import io.circe.generic.auto._
import viewData.GroupDetailedInfoViewData


object GroupList extends Route[GroupListRequest, GroupListResponse] {
  override val route: String = "adminGetGroupList"
}

//REQ
case class GroupListRequest(token:String) extends WithToken


//RES
sealed trait GroupListResponse
case class GroupListResponseSuccess(groups: Seq[GroupDetailedInfoViewData]) extends GroupListResponse
case class GroupListResponseFailure() extends GroupListResponse
