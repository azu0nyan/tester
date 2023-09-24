package tester.ui.components


import clientRequests.admin.{AdminCourseListSuccess, GroupListResponseFailure, GroupListResponseSuccess, UnknownAdminCourseListFailure}

import scala.scalajs.js
import slinky.core.*
import slinky.web.html.*
import typings.antd.components.*
import typings.antd.{antdInts, antdStrings}
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import tester.ui.components.TeacherAppLayout.TeacherAppState.CourseEditing
import tester.ui.requests.Request
import typings.antDesignIcons.components.AntdIcon
import typings.csstype.mod.{OverflowYProperty, PositionProperty}
import typings.rcMenu.esInterfaceMod
import typings.react.mod.CSSProperties
import viewData.GroupDetailedInfoViewData

object TeacherAppLayout {
  case class Props(loggedInUser: LoggedInUser, logout: () => Unit)

  def apply(loggedInUser: LoggedInUser, logout: () => Unit): ReactElement = {
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(loggedInUser, logout)))
  }

  sealed trait TeacherAppState
  object TeacherAppState {
    case object WelcomeScreen extends TeacherAppState
    case object AnswerConfirmation extends TeacherAppState
    case object ProblemsEditing extends TeacherAppState
    case class GroupInfo(groupId: String) extends TeacherAppState
    case class GroupResultsTableEdtiting(groupId: String) extends TeacherAppState
    case class CourseEditing(courseTemplate: String) extends TeacherAppState
  }

  import TeacherAppState.*

  val component = FunctionalComponent[Props] { props =>

    val (state, setState) = useState[TeacherAppState](WelcomeScreen)
    val (groups, setGroups) = useState[Seq[viewData.GroupDetailedInfoViewData]](Seq())
    val (courses, setCourses) = useState[Seq[viewData.AdminCourseViewData]](Seq())
    val (leftCollapsed, setLeftCollapsed) = useState[Boolean](false)

    useEffect(() => {
      Request.sendRequest(clientRequests.admin.GroupList, clientRequests.admin.GroupListRequest(props.loggedInUser.token))(
        onComplete = {
          case GroupListResponseSuccess(groups) =>
            setGroups(groups)
          case GroupListResponseFailure() =>
            Notifications.showError(s"Не могу загрузить список групп (501)")
        }, onFailure = t =>
          Notifications.showError(s"Не могу загрузить список групп ${t.toString} (4xx)")
      )
      Request.sendRequest(clientRequests.admin.AdminCourseList, clientRequests.admin.AdminCourseListRequest(props.loggedInUser.token))(
        onComplete = {
          case AdminCourseListSuccess(newCourses) =>
            setCourses(newCourses)
          case UnknownAdminCourseListFailure() =>
            Notifications.showError(s"Не могу загрузить список курсов (501)")
        }, onFailure = t =>
          Notifications.showError(s"Не могу загрузить список курсов ${t.toString} (4xx)")
      )
    }, Seq())


    def siderGroupMenu: ReactElement = {
      Menu().theme(antdStrings.dark).mode(esInterfaceMod.MenuMode.inline) /*.defaultSelectedKeys(js.Array("1"))*/ (
        SubMenu.withKey("main")
          .icon(AntdIcon(typings.antDesignIconsSvg.esAsnTeamOutlinedMod.default))
          .title("Группы")(
            groups.map(group => SubMenu.withKey(group.groupId).title(group.groupTitle)(
              MenuItem.withKey(group.groupId + "about").onClick(_ => setState(GroupInfo(group.groupId)))("О группе"),
              MenuItem.withKey(group.groupId + "results").onClick(_ => setState(GroupResultsTableEdtiting(group.groupId)))("Результаты"),
            ).build)
          ),
        SubMenu.withKey("courses")
          .icon(AntdIcon(typings.antDesignIconsSvg.esAsnCopyrightCircleOutlinedMod.default))
          .title("Курсы")(
            courses.map(course =>
              MenuItem.withKey(course.courseAlias).onClick(_ => setState(CourseEditing(course.courseAlias)))(course.courseTitle).build,
            )),
        MenuItem.withKey("problems")
          .onClick(_ => setState(ProblemsEditing))
          .icon(AntdIcon(typings.antDesignIconsSvg.esAsnCalculatorOutlinedMod.default))("Задачи"),
        MenuItem.withKey("worksChecking")
          .onClick(_ => setState(AnswerConfirmation))
          .icon(AntdIcon(typings.antDesignIconsSvg.esAsnAuditOutlinedMod.default))("Проверка работ"),
        MenuItem.withKey("logout")
          .onClick(_ => props.logout())
          .icon(AntdIcon(typings.antDesignIconsSvg.esAsnLogoutOutlinedMod.default))("Выход")
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
            case WelcomeScreen =>
              Card.bordered(true)
                .style(CSSProperties())(
                  Title.level(antdInts.`1`)("Добро пожаловать"),
                  p("Вас привествует интерфейс учителя на tester.lnmo.ru . Выберите группу или курс для работы в меню слева.")
                )
            case GroupInfo(groupId) =>
              groups.find(_.groupId == groupId) match {
                case Some(gvd) => GroupDetailedInfo(gvd)
                case None => div(s"Группа $groupId не найдена.")
              }
            case GroupResultsTableEdtiting(groupId) =>
              groups.find(_.groupId == groupId) match {
                case Some(gvd) => GroupResultsTable(props.loggedInUser, gvd)
                case None => div(s"Группа $groupId не найдена.")
              }
            case AnswerConfirmation =>
              AnswerConfirmationLayout(props.loggedInUser, groups)
            case ProblemsEditing =>
              div("NOPE")
          }
        )
      ),
      //Layout.Footer()
    )
  }
}



