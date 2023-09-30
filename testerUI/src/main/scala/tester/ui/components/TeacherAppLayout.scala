package tester.ui.components


import clientRequests.admin.{AdminCourseListSuccess, GroupListResponseFailure, GroupListResponseSuccess, NewCourseTemplateRequest, NewCourseTemplateSuccess, NewCourseTemplateUnknownFailure, UnknownAdminCourseListFailure}

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
    case object NewCoursePage extends TeacherAppState
    case class GroupInfo(groupId: String) extends TeacherAppState
    case class GroupResultsTableEditing(groupId: String) extends TeacherAppState
    case class CourseEditing(courseTemplate: String) extends TeacherAppState
  }

  import TeacherAppState.*

  val component = FunctionalComponent[Props] { props =>

    val (state, setState) = useState[TeacherAppState](WelcomeScreen)
    val (groups, setGroups) = useState[Seq[viewData.GroupDetailedInfoViewData]](Seq())
    val (courses, setCourses) = useState[Seq[viewData.ShortCourseTemplateViewData]](Seq())
    val (leftCollapsed, setLeftCollapsed) = useState[Boolean](false)


    val (newCourseAlias, setNewCourseAlias) = useState[String]("")

    def reloadGroups() =
      Request.sendRequest(clientRequests.admin.GroupList, clientRequests.admin.GroupListRequest(props.loggedInUser.token))(
        onComplete = {
          case GroupListResponseSuccess(groups) =>
            setGroups(groups)
          case GroupListResponseFailure() =>
            Notifications.showError(s"Не могу загрузить список групп (501)")
        }, onFailure = t =>
          Notifications.showError(s"Не могу загрузить список групп ${t.toString} (4xx)")
      )

    def reloadCourses() = Request.sendRequest(clientRequests.admin.AdminCourseList, clientRequests.admin.AdminCourseListRequest(props.loggedInUser.token))(
      onComplete = {
        case AdminCourseListSuccess(newCourses) =>
          setCourses(newCourses)
        case UnknownAdminCourseListFailure() =>
          Notifications.showError(s"Не могу загрузить список курсов (501)")
      }, onFailure = t =>
        Notifications.showError(s"Не могу загрузить список курсов ${t.toString} (4xx)")
    )

    useEffect(() => {
      reloadGroups()
      reloadCourses()
    }, Seq())


    def siderGroupMenu: ReactElement = {
      Menu().theme(antdStrings.dark).mode(esInterfaceMod.MenuMode.inline) /*.defaultSelectedKeys(js.Array("1"))*/ (
        SubMenu.withKey("main")
          .icon(AntdIcon(typings.antDesignIconsSvg.esAsnTeamOutlinedMod.default))
          .title("Группы")(
            groups.map(group => SubMenu.withKey(group.groupId).title(group.groupTitle)(
              MenuItem.withKey(group.groupId + "about").onClick(_ => setState(GroupInfo(group.groupId)))("О группе"),
              MenuItem.withKey(group.groupId + "results").onClick(_ => setState(GroupResultsTableEditing(group.groupId)))("Результаты"),
            ).build)
          ),
        SubMenu.withKey("courses")
          .icon(AntdIcon(typings.antDesignIconsSvg.esAsnCopyrightCircleOutlinedMod.default))
          .title("Курсы")(
            MenuItem
              .withKey("addNewCourseMenuKey")
              .onClick(_ => setState((NewCoursePage)))("Новый курс")
              .icon(AntdIcon(typings.antDesignIconsSvg.esAsnSubnodeOutlinedMod.default))
              .build
              +:
              courses.map(course =>
                MenuItem.withKey(course.alias).onClick(_ => setState(CourseEditing(course.alias)))(course.title).build,
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

    def makeNewCourse(): Unit = {
      import clientRequests.admin.NewCourseTemplate
      Request.sendRequest(NewCourseTemplate, NewCourseTemplateRequest(props.loggedInUser.token, newCourseAlias))(
        onComplete = {
          case NewCourseTemplateSuccess() =>
            Notifications.showSuccess(s"Курс создан")
            reloadCourses()
          case NewCourseTemplateUnknownFailure() =>
            Notifications.showError(s"Не могу создать новый курс (501)")
        }, onFailure = t =>
          Notifications.showError(s"Не могу создать новый курс ${t.toString} (4xx)")
      )
    }


    val width = 300
    Layout().hasSider(true)(
      Layout.Sider
        .collapsible(true)
        .collapsed(leftCollapsed)
        .onCollapse((b, _) => setLeftCollapsed(b))
        .collapsedWidth(0)
        .width(width)
        .zeroWidthTriggerStyle(CSSProperties())
        .trigger(null)
        .style(CSSProperties().setHeight("100vh")
          .setPosition(PositionProperty.fixed)
          .setLeft(0).setTop(0).setBottom(0)
          .setOverflowY(OverflowYProperty.auto))(
          siderGroupMenu
        ),
      Layout().style(CSSProperties().setMarginLeft(width))(
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
            case GroupResultsTableEditing(groupId) =>
              groups.find(_.groupId == groupId) match {
                case Some(gvd) => GroupResultsTable(props.loggedInUser, gvd)
                case None => div(s"Группа $groupId не найдена.")
              }
            case AnswerConfirmation =>
              AnswerConfirmationLayout(props.loggedInUser, groups)
            case ProblemsEditing =>
              div("NOPE")
            case NewCoursePage =>
              Card.bordered(true)
                .style(CSSProperties())(
                  Title.level(antdInts.`1`)("Создать новый шаблон курса"),
                  div(
                    Input.value(newCourseAlias).onChange(e => setNewCourseAlias(e.target_ChangeEvent.value)),
                    Button
                      .onClick(_ => makeNewCourse())
                      ("Создать")
                  )
                )
            case CourseEditing(ct) =>
              CourseEditorLoader(props.loggedInUser, ct)
          }
        )
      ),
      //Layout.Footer()
    )
  }
}



