package tester.ui.components


import otsbridge.{CoursePiece, DisplayMe}
import otsbridge.CoursePiece.CoursePiece

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import typings.antd.components._

import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import tester.ui.components.Helpers.SetInnerHtml
import typings.antd.{antdInts, antdStrings}
import typings.react.mod.CSSProperties
import viewData.{PartialCourseViewData, ProblemRefViewData}

object CourseText {
  case class Props(partialCourse: PartialCourseViewData, selectedPiece: CoursePiece, setSelectedProblem: ProblemRefViewData => Unit, setSelectedCoursePiece: CoursePiece => Unit)

  def apply(partialCourse: PartialCourseViewData, selectedPiece: CoursePiece, setSelectedProblem: ProblemRefViewData => Unit, setSelectedCoursePiece: CoursePiece => Unit): ReactElement = {
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(partialCourse, selectedPiece, setSelectedProblem, setSelectedCoursePiece)))
  }

  def cont(r: ReactElement) = div(style := js.Dynamic.literal(
    width = "-webkit-fill-available",
    display = "flex",
    justifyContent = "center"
  ))(
    Card().style(CSSProperties().setMinWidth("400px").setMaxWidth("900px").setMargin(20).setPadding(5))(
      r
    )
  )


  val component = FunctionalComponent[Props] { props =>
    //should ignore DisplayMe
    def genLinkFor(c: CoursePiece): ReactElement = c match {
      case container: CoursePiece.Container => container match {
        case CoursePiece.CourseRoot(title, annotation, childs) =>
          Card
            .title(Title().level(antdInts.`4`)(title))(
              Button().`type`(antdStrings.primary).onClick(_ => props.setSelectedCoursePiece(c))("Открыть курс")
            )
        case CoursePiece.Theme(alias, title, textHtml, childs, displayMe) =>
          Card
            .title(Title().level(antdInts.`4`)(title))(
              Button().`type`(antdStrings.primary).onClick(_ => props.setSelectedCoursePiece(c))("Открыть тему")
            )
        case CoursePiece.SubTheme(alias, title, textHtml, childs, displayMe) =>
          Card
            .title(Title().level(antdInts.`4`)(title))(
              Button().`type`(antdStrings.primary).onClick(_ => props.setSelectedCoursePiece(c))("Открыть подтему")
            )
      }
      case CoursePiece.HtmlToDisplay(alias, displayMe, htmlRaw) =>
        Card(
          Button().`type`(antdStrings.primary).onClick(_ => props.setSelectedCoursePiece(c))("Открыть")
        )
      case CoursePiece.TextWithHeading(alias, heading, bodyHtml, displayMe, displayInContentsHtml) =>
        Card(
          Button().`type`(antdStrings.primary).onClick(_ => props.setSelectedCoursePiece(c))(heading)
        )
      case CoursePiece.Paragraph(alias, bodyHtml, displayMe) =>
        Card(
          Button().`type`(antdStrings.primary).onClick(_ => props.setSelectedCoursePiece(c))("Открыть")
        )
      case CoursePiece.Problem(problemAlias, displayMe, displayInContentsHtml) =>
        props.partialCourse.refByAlias(problemAlias) match {
          case Some(pref) =>
            Card.title(
              Title().level(antdInts.`4`)(s"${pref.title}", ProblemScoreDisplay(pref.score, true, false)))(
              Button().`type`(antdStrings.primary).onClick(_ => props.setSelectedProblem(pref))("Условие")
            )
          case None =>
            div(s"Задача $problemAlias не найдена, сначала учитель должен добавить её в курс.")
        }
    }

    def genForChilds(childs: Seq[CoursePiece]): ReactElement = {
      div(
        childs.map(f => f.displayMe match {
          case DisplayMe.OwnPage => genLinkFor(f)
          case DisplayMe.Inline => matchPiece(f)
        }
        ): _ *)
    }

    //todo add next|prev buttons
    def matchPiece(c: CoursePiece): ReactElement = {
      import typings.betterReactMathjax.components.{MathJax, MathJaxContext}
      c match {
        case container: CoursePiece.Container =>
          container match {
            case CoursePiece.CourseRoot(title, annotation, childs) =>
              div(
                Title().level(antdInts.`2`)(title),
                MathJax(div(dangerouslySetInnerHTML := new SetInnerHtml(annotation))),
                genForChilds(childs)
              )
            case CoursePiece.Theme(alias, title, textHtml, childs, displayMe) =>
              div(
                Title().level(antdInts.`3`)(title),
                MathJax(div(dangerouslySetInnerHTML := new SetInnerHtml(textHtml))),
                genForChilds(childs)
              )
            case CoursePiece.SubTheme(alias, title, textHtml, childs, displayMe) =>
              div(
                Title().level(antdInts.`4`)(title),
                MathJax(div(dangerouslySetInnerHTML := new SetInnerHtml(textHtml))),
                genForChilds(childs)
              )
          }
        case CoursePiece.HtmlToDisplay(alias, displayMe, htmlRaw) =>
          MathJax(div(dangerouslySetInnerHTML := new SetInnerHtml(htmlRaw)))
        case CoursePiece.TextWithHeading(alias, heading, bodyHtml, displayMe, displayInContentsHtml) =>
          div(
            Title().level(antdInts.`4`)(heading),
            MathJax(div(dangerouslySetInnerHTML := new SetInnerHtml(bodyHtml)))
          )
        case CoursePiece.Paragraph(alias, bodyHtml, displayMe) =>
          MathJax(p(dangerouslySetInnerHTML := new SetInnerHtml(bodyHtml)))
        case cp@CoursePiece.Problem(problemAlias, displayMe, displayInContentsHtml) => //todo use display me
          genLinkFor(cp)
      }
    }
    cont(matchPiece(props.selectedPiece))
  }
}

