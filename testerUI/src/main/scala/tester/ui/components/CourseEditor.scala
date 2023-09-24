package tester.ui.components

import clientRequests.admin.{AddProblemToCourseTemplateRequest, AddProblemToCourseTemplateSuccess, AddProblemToCourseTemplateUnknownFailure, AdminCourseInfoRequest, AdminCourseInfoSuccess, CustomCourseUpdateData, ProblemTemplateListRequest, ProblemTemplateListSuccess, RemoveProblemFromCourseTemplateRequest, RemoveProblemFromCourseTemplateSuccess, RemoveProblemFromCourseTemplateUnknownFailure, UnknownUpdateCustomCourseFailure, UpdateCustomCourseRequest, UpdateCustomCourseSuccess}
import otsbridge.AnswerField.ProgramAnswer
import otsbridge.CoursePiece
import otsbridge.CoursePiece.CourseRoot

import scala.scalajs.js
import slinky.core.*
import slinky.core.WithAttrs.build
import slinky.web.html.*
import typings.antd.components.*
import typings.antd.{antdInts, antdStrings}
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import tester.ui.components.CourseEditor.Props
import tester.ui.components.Helpers.SetInnerHtml
import tester.ui.requests.Request
import typings.antDesignIcons.components.AntdIcon
import typings.csstype.mod.{OverflowYProperty, PositionProperty}
import typings.rcMenu.esInterfaceMod
import typings.react.mod.CSSProperties

object CourseEditorLoader {
  case class Props(loggedInUser: LoggedInUser, templateAlias: String)
  def apply(loggedInUser: LoggedInUser, templateAlias: String): ReactElement =
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(loggedInUser, templateAlias)))

  val component = FunctionalComponent[Props] { props =>
    val (courseData, setCourseData) = useState[Option[viewData.CourseTemplateViewData]](None)

    useEffect(() => {
      import clientRequests.admin.AdminCourseInfo
      Request.sendRequest(AdminCourseInfo,
        AdminCourseInfoRequest(props.loggedInUser.token, props.templateAlias))(
        onComplete = {
          case AdminCourseInfoSuccess(data) => setCourseData(Some(data))
          case _ => Notifications.showError(s"Не могу загрузить шаблон курса. (501)")
        },
        onFailure = _ => Notifications.showError(s"Не могу загрузить шаблон курса. 4xx")
      )
    },
      Seq()
    )

    div(
      courseData match {
        case Some(data) =>
          CourseEditor(props.loggedInUser, props.templateAlias, data.description, data.courseData)
        case None =>
          Spin().tip(s"Загрузка курса...").size(antdStrings.large)
      }
    )

  }
}


object CourseEditor {
  case class Props(loggedInUser: LoggedInUser, templateAlias: String, description: String, courseRoot: CourseRoot)
  def apply(loggedInUser: LoggedInUser, templateAlias: String, description: String, courseRoot: CourseRoot): ReactElement =
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(loggedInUser, templateAlias, description, courseRoot)))

  val component = FunctionalComponent[Props] { props =>

    val (description, setDescription) = useState[String](props.description)
    val (root, setRoot) = useState[CourseRoot](props.courseRoot)

    def submitNewDescription(): Unit = {
      import clientRequests.admin.UpdateCustomCourse
      Request.sendRequest(UpdateCustomCourse, UpdateCustomCourseRequest(props.loggedInUser.token, props.templateAlias, CustomCourseUpdateData(Some(description), None)))(
        onComplete = {
          case UpdateCustomCourseSuccess() => Notifications.showSuccess(s"Сохранено")
          case UnknownUpdateCustomCourseFailure() => Notifications.showSuccess(s"Не могу сохранить(501)")
        },
        onFailure = t => {
          t.printStackTrace()
          Notifications.showSuccess(s"Не могу сохранить($t)")
        }
      )
    }

    def submitNewData(cr: CourseRoot): Unit = {
      import clientRequests.admin.UpdateCustomCourse
      Request.sendRequest(UpdateCustomCourse, UpdateCustomCourseRequest(props.loggedInUser.token, props.templateAlias, CustomCourseUpdateData(None, Some(cr))))(
        onComplete = {
          case UpdateCustomCourseSuccess() =>
            Notifications.showSuccess(s"Сохранено")
            setRoot(cr)
          case UnknownUpdateCustomCourseFailure() => Notifications.showSuccess(s"Не могу сохранить(501)")
        },
        onFailure = t => {
          t.printStackTrace()
          Notifications.showSuccess(s"Не могу сохранить($t)")
        }
      )
    }

    val descriptionInput = TextArea
      .rows(5)
      .value(description)
      .onChange(v => setDescription(v.target.toString))

    div(
      Card.bordered(true)
        .style(CSSProperties())(
          div(s"Алиас: ${props.templateAlias}"),
          div(
            "Описание:",
            descriptionInput,
            Button
              .`type`(antdStrings.primary)
              .onClick(_ => submitNewDescription())("Изменить")
          ),
          CourseDataEditor(root, cr => {
            submitNewData(cr)
          })
        )
    )
  }


  object CourseDataEditor {
    case class Props(data: CourseRoot, submitChanges: CourseRoot => Unit)

    def apply(data: CourseRoot, submitChanges: CourseRoot => Unit): ReactElement =
      import slinky.core.KeyAddingStage.build
      build(component.apply(Props(data, submitChanges)))

    val component = FunctionalComponent[Props] { props =>
      div(
        Card.bordered(true)
          .style(CSSProperties())(
            ProgramAceEditor(props.data.alias, props.data.toJsonPretty, Seq(otsbridge.ProgrammingLanguage.Java), str =>
              try {
                //todo refactor ProgramAceEditor
                val a = ProgramAnswer.fromJsom(str)
                val c = CoursePiece.fromJson(a.program)
                props.submitChanges(c)
              } catch {
                case t: Throwable =>
                  Notifications.showError(s"Ошибка при сохранении $t")
                  t.printStackTrace()
              })
          )
      )
    }
  }

  object CourseProblemsEditor {
    case class Props(loggedInUser: LoggedInUser, templateAlias: String)

    def apply(loggedInUser: LoggedInUser, templateAlias: String): ReactElement =
      import slinky.core.KeyAddingStage.build
      build(component.apply(Props(loggedInUser, templateAlias)))

    val component = FunctionalComponent[Props] { props =>
      import typings.antd.libTableInterfaceMod.{ColumnGroupType, ColumnType}

      val (alias, setAlias) = useState[String]("")
      val (problems, setProblems) = useState[Seq[viewData.ProblemTemplateExampleViewData]](Seq())

      def reloadList(): Unit = {
        import clientRequests.admin.ProblemTemplateList
        Request.sendRequest(ProblemTemplateList,
          ProblemTemplateListRequest(props.loggedInUser.token, Seq(ProblemTemplateList.ProblemTemplateFilter.FromCourseTemplate(props.templateAlias))))(
          onComplete = {
            case ProblemTemplateListSuccess(list) => setProblems(list)
            case _ => Notifications.showError(s"Не могу загрузить задачи курса. (501)")
          },
          onFailure = _ => Notifications.showError(s"Не могу загрузить задачи курса. 4xx")
        )
      }


      useEffect(
        () => reloadList(),
        Seq()
      )

      def addAlias(): Unit = {
        import clientRequests.admin.AddProblemToCourseTemplate
        Request.sendRequest(AddProblemToCourseTemplate, AddProblemToCourseTemplateRequest(props.loggedInUser.token, props.templateAlias, alias))(
          onComplete = {
            case AddProblemToCourseTemplateSuccess() =>
              Notifications.showSuccess(s"Добавлено")
              reloadList()
            case AddProblemToCourseTemplateUnknownFailure(_) => Notifications.showError(s"Не могу добавить задачу (501)")
          },
          onFailure = x => Notifications.showError(s"Не могу добавить задачу (4xx)")
        )
      }

      def removeAlias(toRemove: String): Unit = {
        import clientRequests.admin.RemoveProblemFromCourseTemplate
        Request.sendRequest(RemoveProblemFromCourseTemplate, RemoveProblemFromCourseTemplateRequest(props.loggedInUser.token, props.templateAlias, toRemove))(
          onComplete = {
            case RemoveProblemFromCourseTemplateSuccess() =>
              Notifications.showSuccess(s"Удалено")
              reloadList()
            case RemoveProblemFromCourseTemplateUnknownFailure(_) => Notifications.showError(s"Не могу удалить задачу (501)")
          },
          onFailure = _ => Notifications.showError(s"Не могу удалить задачу (4xx)")
        )
      }

      def buildTextCell(d: viewData.ProblemTemplateExampleViewData) =
        div(dangerouslySetInnerHTML := new SetInnerHtml(d.exampleHtml))

      section(
        Input.value(alias).onChange(e => setAlias(e.target.toString)),
        Button()
          .`type`(antdStrings.primary)
          .onClick(e => addAlias())("Добавить"),
        Table[viewData.ProblemTemplateExampleViewData]()
          .bordered(true)
          //        .dataSourceVarargs(toTableItem(a.head, 1))
          .dataSourceVarargs(problems: _ *)
          .columnsVarargs(
            ColumnType[viewData.ProblemTemplateExampleViewData]()
              .setTitle("Алиас")
              .setDataIndex("alias")
              .setKey("alias")
              .setRender((_, tableItem, _) => build(
                div(
                  i(tableItem.alias),
                  Button()
                    .`type`(antdStrings.primary)
                    .onClick(e => removeAlias(tableItem.alias))("Удалить")
              ))),
            ColumnType[viewData.ProblemTemplateExampleViewData]()
              .setTitle("Название")
              .setDataIndex("title")
              .setKey("title")
              .setRender((_, tableItem, _) => build(p(tableItem.title))),
            ColumnType[viewData.ProblemTemplateExampleViewData]()
              .setTitle("Условие")
              .setDataIndex("text")
              .setKey("text")
              .setRender((_, tableItem, _) => build(buildTextCell(tableItem))),
          )
      )
    }
  }
}
