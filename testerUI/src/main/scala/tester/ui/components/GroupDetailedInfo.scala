package tester.ui.components


import clientRequests.admin.{AddCourseToGroupRequest, AddUserToGroupFailure, AddUserToGroupSuccess}

import scala.scalajs.js
import slinky.core.*
import slinky.web.html.*
import typings.antd.components.*
import typings.antd.{antdInts, antdStrings}
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import tester.ui.requests.Request
import typings.react.mod.CSSProperties
import viewData.GroupDetailedInfoViewData

object GroupDetailedInfo {
  case class Props(loggedInUser: LoggedInUser, data:GroupDetailedInfoViewData)
  def apply(loggedInUser: LoggedInUser, data:GroupDetailedInfoViewData): ReactElement = {
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(loggedInUser, data)))
  }

  val component = FunctionalComponent[Props] { props =>
    useEffect(() => {})
    
    //todo remove
    def addCourseToGroup(alias: String) = {
      import clientRequests.admin.AddCourseToGroup
      Request.sendRequest(AddCourseToGroup, AddCourseToGroupRequest(props.loggedInUser.token, alias, props.data.groupId, true))(
          onComplete = {
            case AddUserToGroupSuccess() =>
//              reloadList() todo
              Notifications.showSuccess(s"$alias добавлен к ${props.data.groupTitle} (200)")

            case AddUserToGroupFailure() =>
              Notifications.showError(s"Не могу добавить курс (501)")
          }
      )
    }
    
    
    def courses = {
      val (courseAlias, setCourseAlias) = useState[String]("")
      div(
        props.data.courses.map(_.title).mkString(", "),
        Input.value(courseAlias).onChange(e => setCourseAlias(e.target_ChangeEvent.value)),
        Button().onClick(_ => addCourseToGroup(courseAlias))("Добавить курс"),
      )
    }

    Card()
      .title(s"Группа ${props.data.groupTitle}")
      .bordered(true)
      .style(CSSProperties())(
        Descriptions()
          .layout(antdStrings.vertical)
          .column(1d)          (
            Descriptions.Item().label("ID")(props.data.groupId),
            Descriptions.Item().label("Название")(props.data.groupTitle),
            Descriptions.Item().label("Описание")(props.data.description),
            Descriptions.Item().label("Курсы")(courses),            
            Descriptions.Item().label("Пользователи")(props.data.users.map(_.loginNameString).mkString(", ")),            
          )
      )
  }
}



