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
  case class Props(loggedInUser: LoggedInUser, onClick: viewData.UserViewData => Unit, buttonText: String)
  def apply(loggedInUser: LoggedInUser, onClick: viewData.UserViewData => Unit, buttonText: String): ReactElement =
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(loggedInUser, onClick, buttonText)))

  val component = FunctionalComponent[Props] { props =>

    val (users, setUsers) = useState[Seq[viewData.UserViewData]](Seq())
    val (text, setText) = useState[String]("")
    val (selectedUser, setSelectedId) = useState[String]("")

    def options: OptionsType = js.Array(users.map(u => OptionData.apply(u.loginNameString).setValue(u.loginNameString)): _ *)

    val input = AutoComplete
      .style(CSSProperties().setWidth("200px"))
      .allowClear(true)
      .autoFocus(true)
      .defaultActiveFirstOption(true)
      .options(options)
      .value(text)
      .onChange((s, _) => setText(s))
      .onSelect { (s, _) =>
        setText(s)
        setSelectedId(s)
      }
      .onSearch(text =>
        import clientRequests.admin.UserListRequest
        import clientRequests.admin.*
        Request.sendRequest(UserList,
          UserListRequest(props.loggedInUser.token, Seq(UserList.UserFilter.MatchesRegex(text + "%")), 20))(
          onComplete = {
            case UserListResponseSuccess(list) =>
              setUsers(list)
            case UserListResponseFailure() =>
              Notifications.showError(s"Не могу загрузить список пользователей (501)")
          }
        )
      )

    div(
      input,
      Button
        .`type`(antdStrings.primary)
        .onClick(_ => users.find(_.loginNameString == selectedUser) match
          case Some(user) => props.onClick(user)
          case None =>
        )(props.buttonText)
    )
  }
}

