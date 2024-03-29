package tester.ui.components

import slinky.core.facade.ReactElement
import slinky.web.html.*
import tester.ui.components.Application.{AdminAppState, ApplicationState, TeacherAppState}
import typings.antd.{antdStrings, libSpaceMod}
import typings.csstype.mod.FloatProperty
import typings.csstype.mod.PositionProperty.fixed
import typings.react.mod.CSSProperties
import typings.antd.components.{List as AntList, *}
import viewData.UserViewData

import scala.scalajs.js
import scala.sys.props

object Helpers {

  val customWarningColor = "#fbb03b"
  val customSuccessColor = "#248f24"
  val customErrorColor = "#af0000"


 

  def sizePair(x: Double, y: Double) = scala.scalajs.js.|.from[js.Tuple2[libSpaceMod.SpaceSize, libSpaceMod.SpaceSize], js.Tuple2[libSpaceMod.SpaceSize, libSpaceMod.SpaceSize], libSpaceMod.SpaceSize](js.Tuple2(scala.scalajs.js.|.from(x), scala.scalajs.js.|.from(y)))


  def basicLayout(content: ReactElement, logout: () => Unit, headerContent: ReactElement = div(), user: LoggedInUser, setAppState: ApplicationState => Unit): ReactElement = {
    Space()
      .direction(antdStrings.vertical)
      .style(CSSProperties().setWidth("100%"))
      //      .size(typings.antd.antdStrings.small)
      .size(sizePair(0, 48))(
        Layout()(
//          Helpers.makeHeader(headerContent, logout, user, setAppState),
          content,
//          Layout.Footer(i("Tester(c) 2049-present")),
        )
      )
  }


  /**Для использования с dangerouslySetInnerHTML := new SetInnerHtml(value) */
  class SetInnerHtml(val __html: String) extends js.Object
  
  def headerHeight  = 30
  
  def fixedLeftSiderStyle = CSSProperties()
    .setOverflow("auto")
    .setHeight("100vh")
    .setPosition(fixed)
    .setLeft(0)    
    .setTop(headerHeight)
    .setBottom(0)


  val rightBorderWhiteStyle = CSSProperties()
    .setBorderRight("solid")
    .setBorderRightWidth("10px")
    .setBorderRightColor("white")
  

  def toName(u: UserViewData): String = {
    val f = (u.lastName.getOrElse("").strip() + " " + u.firstName.getOrElse(" ").strip()).strip()
    if (f.isEmpty) u.login else f
  }
}
