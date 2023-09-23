package tester.ui.components


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
import typings.rcSelect.libInterfaceMod.{OptionData, OptionsType}
import typings.react.mod.CSSProperties


object UserSelector {
  case class Props(loggedInUser: LoggedInUser)
  def apply(loggedInUser: LoggedInUser): ReactElement =
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(loggedInUser)))

  val component = FunctionalComponent[Props] { props =>

    val (users, setUsers) = useState[Seq[viewData.UserViewData]](Seq())

    def options: OptionsType = js.Array(users.map(u => OptionData.apply(u.id).setLabel(u.loginNameString).setValue(u.id)) : _ *)

    div(
      AutoComplete
        .style(CSSProperties().setWidth("100%"))
        .allowClear(true)
        .autoFocus(true)
        .defaultActiveFirstOption(true)
        .options(options)
        .value("%")
        .onSearch(text =>
          import clientRequests.admin.UserListRequest
          import clientRequests.admin.*
          Request.sendRequest(UserList,
            UserListRequest(props.loggedInUser.token, Seq(UserList.UserFilter.MatchesRegex(text)), 20))(
            onComplete = {
              case UserListResponseSuccess(list) =>
                setUsers(list)
              case UserListResponseFailure() =>
                Notifications.showError(s"Не могу загрузить список пользователей (501)")
            }
          )
        )
    )
  }
}

