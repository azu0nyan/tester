package tester.ui.components

import otsbridge.CoursePiece
import otsbridge.CoursePiece.CoursePiece

import scala.scalajs.js
import slinky.core.*
import slinky.web.html.*
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.{React, ReactElement}
import slinky.core.facade.ReactContext.RichReactContext
import tester.ui.components.Application.ApplicationState
import tester.ui.components.Helpers.SetInnerHtml
import tester.ui.requests.Request.sendRequest
import typings.rcMenu.esInterfaceMod
import typings.react.mod.CSSProperties
import viewData.{CourseInfoViewData, PartialCourseViewData, ProblemRefViewData, ProblemViewData}
import typings.antd.antdStrings.{dark, large, light, primary}
import typings.antd.{antdStrings, libSpaceMod}
import typings.antd.components.{List as AntList, *}
import typings.csstype.mod.{OverflowYProperty, PositionProperty}
import typings.rcMenu.esInterfaceMod.MenuMode.inline

object CourseLayout {
  case class Props(loggedInUser: LoggedInUser, partialCourse: PartialCourseViewData, logout: () => Unit, setAppState: ApplicationState => Unit, back: () => Unit)
  def apply(loggedInUser: LoggedInUser, partialCourse: PartialCourseViewData, logout: () => Unit, setAppState: ApplicationState => Unit, back: () => Unit): ReactElement = {
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(loggedInUser, partialCourse, logout, setAppState, back)))
  }


  case class LoadedProblemData(pvd: ProblemViewData, answerInField: String)

  sealed trait DisplayAppMode
  case object DisplayCourseMode extends DisplayAppMode
  case object DisplayProblemMode extends DisplayAppMode
  case object DisplayCourseAndProblem extends DisplayAppMode

  val component = FunctionalComponent[Props] { props =>

    val (leftCollapsed, setLeftCollapsed) = useState[Boolean](false)
    val (rightCollapsed, setRightCollapsed) = useState[Boolean](true)

    val (appMode, setAppMode) = useState[DisplayAppMode](DisplayCourseMode)
    val (loadedProblems, setLoadedProblems) = useState[Map[String, LoadedProblemData]](Map[String, LoadedProblemData]())

    val (selectedProblem, setSelectedProblemInner) = useState[Option[ProblemRefViewData]](None)
    val (selectedCoursePiece, setSelectedCoursePieceInner) = useState[CoursePiece](props.partialCourse.courseData)

    def setSelectedProblem(pref: Option[ProblemRefViewData]): Unit = {
      setSelectedProblemInner(pref)
      (pref, appMode) match {
        case (Some(_), DisplayCourseMode) => setAppMode(DisplayProblemMode)
        case (None, DisplayProblemMode) => setAppMode(DisplayCourseMode)
        case _ =>
      }
    }

    def setSelectedCoursePiece(cp: CoursePiece): Unit = {
      setSelectedCoursePieceInner(cp)
      appMode match {
        case DisplayProblemMode => setAppMode(DisplayCourseMode)
        case _ =>
      }
    }

    def onProblemLoaded(ref: ProblemRefViewData, problemViewData: ProblemViewData): Unit = {
      setLoadedProblems(old => {
        old.get(ref.templateAlias) match {
          case Some(loadedData) => old + (ref.templateAlias -> loadedData.copy(pvd = problemViewData))
          case None => old + (ref.templateAlias -> LoadedProblemData(problemViewData, problemViewData.answers.lastOption.map(_.answerText).getOrElse("")))
        }
      })
    }
    //
    //    def saveAnswer(ref: ProblemRefViewData, newAnswer: String): Unit = {
    //      println(s"Saving answer $newAnswer")
    //      setLoadedProblems(old =>
    //        old + (ref.templateAlias -> old(ref.templateAlias).copy(answerInField = newAnswer))
    //      )
    //    }

    def displayCourse(): ReactElement =
      CourseText(props.partialCourse, selectedCoursePiece, p => setSelectedProblem(Some(p)), cp => setSelectedCoursePiece(cp))

    def displayProblem(): ReactElement =
      selectedProblem match {
        case Some(problemRef) =>
          loadedProblems.get(problemRef.templateAlias) match {
            case Some(loadedData) => ProblemPage(props.loggedInUser, loadedData, () => {
              sendRequest(clientRequests.ProblemData, clientRequests.ProblemDataRequest(props.loggedInUser.token, problemRef.problemId))(onComplete = {
                case clientRequests.ProblemDataSuccess(pwd) => onProblemLoaded(problemRef, pwd)
                case clientRequests.UnknownProblemDataFailure() => Notifications.showError(s"Не могу загрузить задачу")
              })
            }) //.withKey(problemRef.problemId) todo ????
            case None =>
              ProblemLoader(props.loggedInUser, problemRef.problemId, p => onProblemLoaded(problemRef, p))
          }
        case None =>
          div("Выберите задачу")
      }

    def displayContent(): ReactElement = Layout.Content()(appMode match {
      case DisplayCourseMode => displayCourse()
      case DisplayProblemMode => displayProblem()
      case DisplayCourseAndProblem =>
        Row().wrap(true)(
          Col()
            .flex("1 1 300px")(
              Card().style(CSSProperties().setMinWidth("300px").setMaxWidth("900px").setMargin(20).setPadding(5))(
                displayCourse()
              )
            ),
          Col()
            .flex("1 1 400px")(
              Card().style(CSSProperties().setMinWidth("400px").setMaxWidth("900px").setMargin(20).setPadding(5))(
                displayProblem()
              )
            )
        )
      case _ => println(s"Unknown mode $appMode")
        div()
    })

    import typings.antDesignIcons.components.AntdIcon
    def leftSiderHeader =
      Menu().theme(light).mode(inline).selectable(false)
        .style(CSSProperties().setMarginTop("5px"))(
          MenuItem
            .withKey("menuHeader")("Оглавление")
            .onClick(_ => setLeftCollapsed(true))
            .icon(AntdIcon(typings.antDesignIconsSvg.esAsnBackwardFilledMod.default))
            .build
        )

    val leftSider =
      Layout.Sider()
        .collapsible(true)
        .collapsed(leftCollapsed)
        .collapsedWidth(0)
        .width(300)
        .zeroWidthTriggerStyle(CSSProperties().setTop("0px"))
        .trigger(if (leftCollapsed) AntdIcon(typings.antDesignIconsSvg.esAsnForwardFilledMod.default) else null)
        .onCollapse((b, _) => setLeftCollapsed(b))(
          leftSiderHeader,
          CourseContents(props.partialCourse, cp => setSelectedCoursePiece(cp), () => props.back())
        )


    def rightSiderHeader =
      Menu().theme(light).mode(inline).selectable(false)
        .style(CSSProperties().setMarginTop("5px"))(
          MenuItem
            .withKey("menuProgress")("Прогресс")
            .onClick(_ => setRightCollapsed(true))
            .icon(AntdIcon(typings.antDesignIconsSvg.esAsnBackwardFilledMod.default))
            .build
        )

    val rightSider =
      Layout.Sider()
        .collapsible(true)
        .collapsed(rightCollapsed)
        .collapsedWidth(0)
        .width(300)
        .zeroWidthTriggerStyle(CSSProperties().setRight("0px").setTop("0px"))
        .trigger(if (rightCollapsed) AntdIcon(typings.antDesignIconsSvg.esAsnBackwardFilledMod.default) else null)
        .onCollapse((b, _) => setRightCollapsed(b))(
          rightSiderHeader,
          CourseProblemSelector(props.partialCourse, spRef => setSelectedProblem(Some(spRef)))
        )


    val content = Layout().style(CSSProperties().setMinHeight("100vh"))(
      leftSider,
      displayContent(),
      rightSider
    )

    val headerContent = Button().`type`(primary).onClick(_ => appMode match {
      case DisplayCourseMode => setAppMode(DisplayCourseAndProblem)
      case DisplayProblemMode => setAppMode(DisplayCourseAndProblem)
      case DisplayCourseAndProblem =>
        if (selectedProblem.nonEmpty) setAppMode(DisplayProblemMode)
        else setAppMode(DisplayCourseMode)
    })(appMode match {
      case DisplayCourseMode => "Двойное отображение"
      case DisplayProblemMode => "Двойное отображение"
      case DisplayCourseAndProblem => "Одинарное отображение"
    })


    //    Helpers.basicLayout(content, props.logout, headerContent, props.loggedInUser, props.setAppState)
    content
  }


  object ProblemLoader {
    case class Props(loggedInUser: LoggedInUser, problemId: String, onLoad: ProblemViewData => Unit)
    def apply(loggedInUser: LoggedInUser, problemId: String, onLoad: ProblemViewData => Unit): ReactElement = {
      import slinky.core.KeyAddingStage.build
      build(component.apply(Props(loggedInUser, problemId, onLoad)))
    }

    val component = FunctionalComponent[Props] { props =>
      useEffect(() => {
        sendRequest(clientRequests.ProblemData, clientRequests.ProblemDataRequest(props.loggedInUser.token, props.problemId))(onComplete = {
          case clientRequests.ProblemDataSuccess(pwd) => props.onLoad(pwd)
          case clientRequests.UnknownProblemDataFailure() => Notifications.showError(s"Не могу загрузить задачу")
        })
      })
      Space().style(CSSProperties().setWidth("100%").setHeight("100%").setJustifyContent("center"))(
        Spin().tip(s"Загрузка задачи...").size(large)
      )
    }
  }
}
