package tester.ui.components

import DbViewsShared.CourseStatus
import clientRequests.CoursesList
import slinky.core.KeyAddingStage.build

import scala.scalajs.js
import slinky.core.*
import slinky.web.html.*
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import tester.ui.DateFormat
import tester.ui.components.Application.ApplicationState
import tester.ui.requests.Request.sendRequest
import typings.antDesignIcons.components.AntdIcon
import typings.antd.antdStrings.{dark, large, primary}
import typings.antd.{antdStrings, libSpaceMod}
import typings.antd.components.{List as AntList, *}
import typings.csstype.mod.{OverflowYProperty, PositionProperty}
import typings.rcMenu.esInterfaceMod.MenuMode.inline
import typings.react.mod.CSSProperties
import viewData.{PartialCourseViewData, ProblemRefViewData, UserViewData}
import viewData.CourseInfoViewData


object CourseSelectionLayout {
  case class Props(loggedInUser: LoggedInUser, onSelected: CourseInfoViewData => Unit, setAppState: ApplicationState => Unit, logout: () => Unit)
  def apply(loggedInUser: LoggedInUser, onSelected: CourseInfoViewData => Unit, setAppState: ApplicationState => Unit, logout: () => Unit): ReactElement =
    build(component.apply(Props(loggedInUser, onSelected, setAppState, logout)))

  val component = FunctionalComponent[Props] { props =>

    val (coursesList, setCoursesList) = useState[Seq[CourseInfoViewData]](Seq())
    val (selectedCourse, setSelectedCourse) = useState[Option[CourseInfoViewData]](None)
    val (loaded, setLoaded) = useState[Boolean](false)
    useEffect(() => {
      sendRequest(CoursesList, clientRequests.CoursesListRequest(props.loggedInUser.token, props.loggedInUser.userViewData.id))(onComplete = {
        case clientRequests.GetCoursesListSuccess(courses) =>
          setCoursesList(courses.existing)
          setSelectedCourse(courses.existing.headOption)
          setLoaded(true)
        case clientRequests.GetCoursesListFailure(fal) => //todo
      })
    }, Seq())


    def rightSider(l: LoggedInUser) = {
      Layout.Sider()
        .style(CSSProperties()
          //      .setOverflow(OverflowBlockProperty.auto)
          //      .setHeight("100vh")
          //      .setPosition(PositionProperty.fixed)
          //      .setLeft(0)
          //      .setTop(0)
          //      .setBottom(0)
        )(UserInfoBox(l))
    }

    def controlMenuItems = Seq(
      scala.Option.when(props.loggedInUser.isTeacher)(
        MenuItem
          .withKey("goToTeacher")("В учительскую")
          .onClick(_ => props.setAppState(Application.TeacherAppState))
          .icon(AntdIcon(typings.antDesignIconsSvg.esAsnExperimentOutlinedMod.default))
          .build
      ),
      scala.Option.when(props.loggedInUser.isAdmin)(
        MenuItem
          .withKey("goToAdmin")("В админскую")
          .onClick(_ => props.setAppState(Application.AdminAppState))
          .icon(AntdIcon(typings.antDesignIconsSvg.esAsnDatabaseOutlinedMod.default))
          .build
      ),
      Some(MenuItem
        .withKey("logout")("Выйти")
        .onClick(_ => props.logout())
        .icon(AntdIcon(typings.antDesignIconsSvg.esAsnLogoutOutlinedMod.default))
        .build)
    ).flatten.toSeq


    val leftSiderWidth = 300

    def leftSider(coursesList: Seq[CourseInfoViewData], setSelected: CourseInfoViewData => Unit) = {
      Layout.Sider()
        .width(leftSiderWidth)
        .style(CSSProperties().setHeight("100vh")
          .setPosition(PositionProperty.fixed)
          .setLeft(0).setTop(0).setBottom(0)
          .setOverflowY(OverflowYProperty.auto))(
          Menu().theme(dark).mode(inline) /*.defaultSelectedKeys(js.Array("1"))*/ (
            (coursesList.map(course => MenuItem.withKey(course.courseId)(course.title).onClick(_ => setSelected(course)).build)
              ++ controlMenuItems): _ *
          )
        )
    }

    def content(selectedCourse: Option[CourseInfoViewData]) = {
      Layout.Content()
        .style(CSSProperties())(
          selectedCourse match {
            case Some(course) =>
              Card()
              .style(CSSProperties().setMargin(20))
              .title(h1(course.title))(
                p(course.description),
                p(course.status match {
                  case CourseStatus.Passing(endsAt) => endsAt match {
                    case Some(value) => div("Активен до " + DateFormat.dateFormatter.format(value))
                    case None => div("Активен")
                  }
                  case CourseStatus.Finished() => div("Завершен")
                }),
                Button()
                  .`type`(primary)
                  .onClick(e => props.onSelected(course))("Продолжить ")
              )
            case None =>
              if(loaded) Card()
                .style(CSSProperties().setMargin(20))
                .title("Курсы не найдены")("Если тут должны быть курсы, а их нет, обратитесь к вашему преподавателю по програраммированию.")
              else  Spin().tip(s"Загрузка списка курсов...").size(large)
          }
        )

    }


    Layout().hasSider(true)(
      leftSider(coursesList, x => setSelectedCourse(Some(x))),
      Layout().style(CSSProperties().setMarginLeft(leftSiderWidth))(
        content(selectedCourse),
        rightSider(props.loggedInUser)
      )
    )
  }
}
