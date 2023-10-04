package tester.ui.components

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import slinky.core.facade.Hooks.useState
import slinky.core.facade.ReactElement
import tester.ui.components.Application.ApplicationState
import typings.antd.{antdStrings, libSpaceMod}
import typings.antd.components.{List => AntList, _}
import typings.csstype.mod.FloatProperty
import typings.react.mod.CSSProperties
import viewData.{CourseInfoViewData, PartialCourseViewData, ProblemRefViewData, UserViewData}

object UserAppLayout {


  case class Props(loggedInUser: LoggedInUser, logout: () => Unit, setAppState: ApplicationState => Unit)

  def apply(loggedInUser: LoggedInUser, logout: () => Unit, setAppState: ApplicationState => Unit): ReactElement = {
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(loggedInUser, logout, setAppState)))
  }


  val component = FunctionalComponent[Props] { props =>
    val (selectedCourse, setSelectedCourse) = useState[Option[CourseInfoViewData]](None)

    selectedCourse match {
      case Some(course) => CourseLoaderLayout(props.loggedInUser, course, props.logout, props.setAppState, () => setSelectedCourse(None))
      case None =>
        CourseSelectionLayout(
          loggedInUser = props.loggedInUser,
          onSelected = s => setSelectedCourse(Some(s)),
          setAppState = props.setAppState,
          logout = props.logout,
        )
    }

  }
}
