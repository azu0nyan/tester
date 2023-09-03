package tester.ui.components


import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import typings.antd.components._
import typings.antd.{antdInts, antdStrings}
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import typings.react.mod.CSSProperties
import viewData.GroupDetailedInfoViewData

object GroupDetailedInfo {
  case class Props(data:GroupDetailedInfoViewData)
  def apply(data:GroupDetailedInfoViewData): ReactElement = {
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(data)))
  }

  val component = FunctionalComponent[Props] { props =>
    // val (name, setName) = useState[T](t)

    useEffect(() => {})

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
            Descriptions.Item().label("Курсы")(props.data.courses.map(_.title).mkString(", ")),            
            Descriptions.Item().label("Пользователи")(props.data.users.map(_.loginNameString).mkString(", ")),            
          )
      )
  }
}



