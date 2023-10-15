package tester.ui.components

import scala.scalajs.js
import slinky.core._
import slinky.core.facade.ReactElement
import slinky.web.ReactDOM
import slinky.web.html._
import typings.antd.antdStrings
import typings.antd.components.{List => AntList, _}
import typings.react.mod.CSSProperties
import viewData.UserViewData

object UserInfoBox {

  val defaultStyle = CSSProperties().setMaxWidth("200px")
  case class Props(l: LoggedInUser, style: CSSProperties = defaultStyle) {
    def u: UserViewData = l.userViewData
  }
  def apply(u: LoggedInUser, style: CSSProperties = defaultStyle): ReactElement = {
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(u, style)))
  }

  val component = FunctionalComponent[Props] { props =>

    Card()
      .title("Профиль")
      .bordered(true)
      .style(props.style)(
        Descriptions()
          .layout(antdStrings.vertical)
          .column(1d)
          .size(antdStrings.small)(
            Descriptions.Item().label("Логин")(props.u.login),
            Descriptions.Item().label("Имя")(props.u.firstName.getOrElse("").asInstanceOf[String]),
            Descriptions.Item().label("Фамилия")(props.u.lastName.getOrElse("").asInstanceOf[String]),
            Descriptions.Item().label("Почта")(props.u.email.getOrElse("").asInstanceOf[String]),
            Descriptions.Item().label("Дата регистрации")(props.u.registeredAt.toString),
            Descriptions.Item().label("Роль") {
              if (!props.l.isTeacher && !props.l.isTeacher) "Ученик"
              else if (props.l.isAdmin) "Админ"
              else "Учитель"
            },
            Descriptions.Item().label("Группы")(props.u.groups.map(_.groupTitle).mkString(", ")),
          )
      )

  }


}
