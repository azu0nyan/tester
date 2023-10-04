package tester.ui.components.admin

import slinky.core.*
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import slinky.web.html.*
import tester.ui.components.Application.{ApplicationState, StudentAppState, TeacherAppState}
import tester.ui.components.LoggedInUser
import tester.ui.requests.Request
import typings.antDesignIcons.components.AntdIcon
import typings.antd.components.*
import typings.antd.{antdInts, antdStrings}
import typings.csstype.mod.{OverflowYProperty, PositionProperty}
import typings.rcMenu.esInterfaceMod
import typings.react.mod.CSSProperties

import scala.scalajs.js

object AdminAppLayout {
  case class Props(loggedInUser: LoggedInUser, logout: () => Unit, setAppState: ApplicationState => Unit)

  def apply(loggedInUser: LoggedInUser, logout: () => Unit, setAppState: ApplicationState => Unit): ReactElement = {
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(loggedInUser, logout, setAppState)))
  }

  sealed trait AdminAppState
  object AdminAppState {
    case object WelcomeScreenAdminAppState extends AdminAppState
    case object UserListAdminAppState extends AdminAppState
    case object BatchRegisterAdminAppState extends AdminAppState
    case object GroupListAdminAppState extends AdminAppState
  }
  import AdminAppState.*


  val component = FunctionalComponent[Props] { props =>

    val (state, setState) = useState[AdminAppState](WelcomeScreenAdminAppState)
    val (leftCollapsed, setLeftCollapsed) = useState[Boolean](false)

    useEffect(() => {
      
    }, Seq())


    def siderGroupMenu: ReactElement = {
      Menu().theme(antdStrings.dark).mode(esInterfaceMod.MenuMode.inline) /*.defaultSelectedKeys(js.Array("1"))*/ (
        MenuItem.withKey("userList").onClick(_ => setState(UserListAdminAppState)).icon(AntdIcon(typings.antDesignIconsSvg.esAsnUserOutlinedMod.default))("Пользователи"),
        MenuItem.withKey("batchRegistration").onClick(_ => setState(BatchRegisterAdminAppState)).icon(AntdIcon(typings.antDesignIconsSvg.esAsnUserAddOutlinedMod.default))("Регистрация"),
        MenuItem.withKey("groupList").onClick(_ => setState(GroupListAdminAppState)).icon(AntdIcon(typings.antDesignIconsSvg.esAsnUsergroupAddOutlinedMod.default))("Группы"),
        MenuItem.withKey("teachers").onClick(_ => props.setAppState(TeacherAppState)).icon(AntdIcon(typings.antDesignIconsSvg.esAsnExperimentOutlinedMod.default))("В учительскую"),
        MenuItem.withKey("users").onClick(_ => props.setAppState(StudentAppState)).icon(AntdIcon(typings.antDesignIconsSvg.esAsnSmileOutlinedMod.default))("Вид от пользователя"),
        MenuItem.withKey("logout").onClick(_ => props.logout()).icon(AntdIcon(typings.antDesignIconsSvg.esAsnLogoutOutlinedMod.default))("Выход"),
      )
    }


    Layout().hasSider(true)(
      Layout.Sider
        .collapsible(true)
        .collapsed(leftCollapsed)
        .onCollapse((b, _) => setLeftCollapsed(b))
        .collapsedWidth(0)
        .zeroWidthTriggerStyle(CSSProperties())
        .trigger(null)
        .style(CSSProperties().setHeight("100vh")
          .setPosition(PositionProperty.fixed)
          .setLeft(0).setTop(0).setBottom(0)
          .setOverflowY(OverflowYProperty.auto))(
          siderGroupMenu
        ),
      Layout().style(CSSProperties().setMarginLeft(200))(
        //header
        Layout.Content(
          state match {
            case WelcomeScreenAdminAppState =>
              Card.bordered(true)
                .style(CSSProperties())(
                  Title.level(antdInts.`1`)("Добро пожаловать снова"),
                  p("Ты в админке, тут все свои.")
                )
            case UserListAdminAppState =>
              AdminUserList(props.loggedInUser)
            case GroupListAdminAppState =>
              AdminGroupList(props.loggedInUser)
            case BatchRegisterAdminAppState =>
              AdminBatchRegister(props.loggedInUser)
          }
        )
      ),

    )
  }

}