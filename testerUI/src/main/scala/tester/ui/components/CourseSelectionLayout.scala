package tester.ui.components

import DbViewsShared.CourseStatus
import clientRequests.CoursesList
import slinky.core.KeyAddingStage.build

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import tester.ui.DateFormat
import tester.ui.requests.Request.sendRequest
import typings.antd.antdStrings.{dark, large, primary}
import typings.antd.{antdStrings, libSpaceMod}
import typings.antd.components.{List => AntList, _}
import typings.rcMenu.esInterfaceMod.MenuMode.inline
import typings.react.mod.CSSProperties
import viewData.{PartialCourseViewData, ProblemRefViewData, UserViewData}
import viewData.CourseInfoViewData


object CourseSelectionLayout {
  case class Props(loggedInUser: LoggedInUser, onSelected: CourseInfoViewData => Unit)
  def apply(loggedInUser: LoggedInUser, onSelected: CourseInfoViewData => Unit): ReactElement =
    build(component.apply(Props(loggedInUser, onSelected)))

  val component = FunctionalComponent[Props] { props =>

    def rightSider(u: UserViewData) = {
      Layout.Sider()
        .style(CSSProperties()
          //      .setOverflow(OverflowBlockProperty.auto)
          //      .setHeight("100vh")
          //      .setPosition(PositionProperty.fixed)
          //      .setLeft(0)
          //      .setTop(0)
          //      .setBottom(0)
        )(UserInfoBox(u))
    }

    def leftSider(coursesList: Seq[CourseInfoViewData], setSelected: CourseInfoViewData => Unit) = {
      Layout.Sider()(coursesList match {
        case Seq() =>  Spin().tip(s"Загрузка списка курсов...").size(large)
        case courses =>
          Menu().theme(dark).mode(inline) /*.defaultSelectedKeys(js.Array("1"))*/ (
            courses.map(course => MenuItem("")(course.title).onClick(_ => setSelected(course)).build)
          )
      })
    }

    def content(courses: Seq[CourseInfoViewData], selectedCourse: Option[CourseInfoViewData]) = {
      Layout.Content()
        .style(CSSProperties())(
          selectedCourse match {
            case Some(course) => Card().style(CSSProperties().setMargin(20))(
              h1(course.title),
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
            case None => div("Выберите курс")
          }
        )

    }


    val (coursesList, setCoursesList) = useState[Seq[CourseInfoViewData]](Seq())
    val (selectedCourse, setSelectedCourse) = useState[Option[CourseInfoViewData]](None)
    useEffect(() => {
      sendRequest(CoursesList, clientRequests.CoursesListRequest(props.loggedInUser.token, props.loggedInUser.userViewData.id))(onComplete = {
        case clientRequests.GetCoursesListSuccess(courses) =>
          setCoursesList(courses.existing)
          setSelectedCourse(courses.existing.headOption)
        case clientRequests.GetCoursesListFailure(fal) => //todo
      })
    }, Seq())



    Layout()(
      leftSider(coursesList, x => setSelectedCourse(Some(x))),
      content(coursesList, selectedCourse),
      rightSider(props.loggedInUser.userViewData)
    )
  }
}
