package tester.ui.components.admin

import slinky.core.*
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import slinky.web.html.*
import tester.ui.components.{LoggedInUser, UserSelector}
import tester.ui.requests.Request
import typings.antDesignIcons.components.AntdIcon
import typings.antd.components.*
import typings.antd.{antdInts, antdStrings}
import typings.csstype.mod.{OverflowYProperty, PositionProperty}
import typings.rcMenu.esInterfaceMod
import typings.react.mod.CSSProperties

import scala.scalajs.js

object AdminUserList {
  case class Props(loggedInUser: LoggedInUser)
  def apply(loggedInUser: LoggedInUser): ReactElement =
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(loggedInUser)))

  val component = FunctionalComponent[Props] { props =>
    div(
      Card.bordered(true)
        .style(CSSProperties())(
          UserSelector(props.loggedInUser, u => println(u), "Under construction...")
        )
    )
  }
}
