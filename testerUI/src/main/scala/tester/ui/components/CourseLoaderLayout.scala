package tester.ui.components

import clientRequests.PartialCourseData

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.{React, ReactElement}
import slinky.core.facade.ReactContext.RichReactContext
import tester.ui.components.Application.ApplicationState
import tester.ui.requests.Request.sendRequest
import typings.antd.antdStrings.large
import typings.antd.components.{List => AntList, _}
import viewData.{CourseInfoViewData, PartialCourseViewData}


object CourseLoaderLayout {
  case class Props(loggedInUser: LoggedInUser, courseInfo: CourseInfoViewData, logout: () => Unit, setAppState: ApplicationState => Unit)
  def apply(loggedInUser: LoggedInUser, courseInfo: CourseInfoViewData, logout: () => Unit, setAppState: ApplicationState => Unit):  ReactElement = {
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(loggedInUser, courseInfo, logout, setAppState)))
  }


  val component = FunctionalComponent[Props] { props =>
    val (courseData, setCourseData) = useState[Option[PartialCourseViewData]](None)

    useEffect(() => {
      sendRequest(PartialCourseData, clientRequests.PartialCourseDataRequest(props.loggedInUser.token, props.courseInfo.courseId))(onComplete = {
        case clientRequests.PartialCourseDataSuccess(data) =>
          println(data)
          setCourseData(Some(data))
        case clientRequests.PartialCourseNotOwnedByYou() => Notifications.showError(s"Это не ваш курс") //todo
        case clientRequests.PartialCourseNotFound() => Notifications.showError(s"Курс не найден") //todo
        case clientRequests.PartialCourseDataFailure(clientRequests.BadToken()) => Notifications.showError(s"Полохой токен") //todo
        case clientRequests.PartialCourseDataFailure(fal) => Notifications.showError(s"Ошибка 501") //todo
      }, onFailure = {
        x =>
          x.printStackTrace()
          Notifications.showError(s"Ошибка клиента")
      })
    }, Seq())

    courseData match {
      case Some(p) =>
        DisplayPartialCourse(props.loggedInUser, p, props.logout, props.setAppState)
      case None =>
        Helpers.basicLayout(Spin().tip(s"Загрузка курса...").size(large), props.logout, div(), props.loggedInUser, props.setAppState)
    }
  }


}
