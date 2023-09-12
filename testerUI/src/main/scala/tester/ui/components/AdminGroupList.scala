package tester.ui.components

import clientRequests.admin.{GroupListRequest, GroupListResponseSuccess}

import scala.scalajs.js
import slinky.core.*
import slinky.web.html.*
import typings.antd.components.*
import typings.antd.{antdInts, antdStrings}
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import tester.ui.requests.Request
import typings.antDesignIcons.components.AntdIcon
import typings.csstype.mod.{OverflowYProperty, PositionProperty}
import typings.rcMenu.esInterfaceMod
import typings.react.mod.CSSProperties
import viewData.GroupDetailedInfoViewData

object AdminGroupList {
  case class Props(loggedInUser: LoggedInUser)
  def apply(loggedInUser: LoggedInUser): ReactElement =
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(loggedInUser)))

  val component = FunctionalComponent[Props] { props =>

    val (groups, setGroups) = useState[Seq[GroupDetailedInfoViewData]](Seq())

    useEffect(() => {
      Request.sendRequest(clientRequests.admin.GroupList, GroupListRequest(props.loggedInUser.token))(
        onComplete = {
          case GroupListResponseSuccess(groups) =>
            setGroups(groups)
          case GroupListResponseFailure() =>
            Notifications.showError(s"Не могу загрузить список групп (501)")
        }
      )
    }, Seq())


    div()
  }
}


