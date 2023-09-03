package tester.ui.components

import otsbridge.ProblemScore
import otsbridge.ProblemScore.ProblemScore

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import typings.antd.antdStrings
import typings.antd.components.Progress
import typings.antd.libProgressProgressMod.ProgressSize
import typings.react.mod.CSSProperties

object ProblemScoreDisplay {

  case class Props(ps: ProblemScore, hasAnswers: Boolean, haveWaitingConfirmAnswer: Boolean)


  def apply(ps: ProblemScore, hasAnswers: Boolean, haveWaitingConfirmAnswer: Boolean): ReactElement = {
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(ps, hasAnswers, haveWaitingConfirmAnswer)))
  }

  def problemScoreElement(text: String, pct: Double, color: String) =
    Progress()
      .`type`(antdStrings.line)
      .percent(pct * 100)
      //      .size(scalajs.js.Tuple2(4d, 100d).asInstanceOf[ProgressSize])
      .format((_, _) => div(b(style := js.Dynamic.literal(color = color))(text)))
      .strokeColor(color)

  def acceptNotAcceptText(passed: Boolean) =
    if (passed) b(style := js.Dynamic.literal(color = Helpers.customSuccessColor))("Зачтено")
    else b(style := js.Dynamic.literal(color = Helpers.customErrorColor))("Не зачтено")


  val component = FunctionalComponent[Props] { props =>
    //    val text = props.ps match {
    //      case ProblemScore.BinaryScore(passed) => if(passed) "Зачтено" else "Не зачтено"
    //      case _ => props.ps.toPrettyString
    //    }
    //
    div(style := js.Dynamic.literal(
      display = "flex",
      justifyContent = "center",
//      width = "90%"
    ))(if (props.ps.isMax) {
      props.ps match {
        case ProblemScore.BinaryScore(passed) => acceptNotAcceptText(passed)
        case _ => problemScoreElement(props.ps.toPrettyString, props.ps.percentage, Helpers.customSuccessColor)
      }
    } else if (props.haveWaitingConfirmAnswer) {
      div(style := js.Dynamic.literal(
        color = Helpers.customWarningColor
      ))(b("Ожидает подтверждаения преподавателем"))
    } else if (props.ps.toInt == 0 && !props.hasAnswers) {
      div(style := js.Dynamic.literal(
        color = Helpers.customErrorColor
      ))(b("Нет ответа"))
    } else {
      props.ps match {
        case ProblemScore.BinaryScore(passed) => acceptNotAcceptText(passed)
        case _ => problemScoreElement(props.ps.toPrettyString, props.ps.percentage, if (props.ps.toInt == 0) Helpers.customErrorColor else Helpers.customWarningColor)
      }
    })


  }
}
