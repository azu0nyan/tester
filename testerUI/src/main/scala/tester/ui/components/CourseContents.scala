package tester.ui.components


import otsbridge.CoursePiece
import otsbridge.CoursePiece.{Container, CoursePiece}

import scala.scalajs.js
import slinky.core.*
import slinky.web.html.*
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import typings.StBuildingComponent
import typings.antDesignIcons.components.AntdIcon
import typings.antd.antdStrings.dark
import typings.antd.components.*
import typings.antd.libMenuMenuItemMod.MenuItem
import viewData.PartialCourseViewData

object CourseContents {
  case class Props(pcvd: PartialCourseViewData, onPieceSelected: CoursePiece => Unit)
  def apply(pcvd: PartialCourseViewData, onPieceSelected: CoursePiece => Unit): ReactElement = {
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(pcvd, onPieceSelected)))
  }

  val component = FunctionalComponent[Props] { props =>
    //    val (name, setName) = useState[T](t)

    def buildMenuFor(container: Container): ReactElement = {

      val (title, content) = container match {
        case CoursePiece.CourseRoot(title, annotation, childs) => (title, annotation)
        case CoursePiece.Theme(alias, title, textHtml, childs, displayMe) => (title, textHtml)
        case CoursePiece.SubTheme(alias, title, textHtml, childs, displayMe) => (title, textHtml)
      }
      //todo make menu item for all with displayMe OwnPage
      if (container.childs.count(_.isInstanceOf[Container]) == 0) {
        MenuItem.withKey(container.alias)(title)
      } else {
        val childs:Seq[ReactElement] =
          (if (content.nonEmpty) Seq[ReactElement](MenuItem.withKey(container.alias)(title)) else Seq[ReactElement]()) ++ (container.childs.flatMap {
          case c: Container => Some(buildMenuFor(c))
          case _ => None
        })

        SubMenu.withKey(container.alias)
          /*.onTitleClick(_ => props.onPieceSelected(props.pcvd.courseData.findByAlias(container.alias).getOrElse(props.pcvd.courseData)))*/
          .title(title)(
            childs: _ *
          )
      }
    }

    useEffect(() => {})




    Menu()
      .onClick(ev => {
        props.onPieceSelected(props.pcvd.courseData.findByAlias(ev.key.toString).getOrElse(props.pcvd.courseData))
      })
      .theme(dark)
      .style(Helpers.rightBorderWhiteStyle)
      .mode(typings.rcMenu.esInterfaceMod.MenuMode.inline) /*.defaultSelectedKeys(js.Array("1"))*/ (
        (props.pcvd.courseData.childs.collect { case c: Container => c }.map(buildMenuFor)) : _ *
      )
  }
}

