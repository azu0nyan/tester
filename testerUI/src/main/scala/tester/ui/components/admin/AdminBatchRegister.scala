package tester.ui.components.admin

import clientRequests.RegistrationRequest
import clientRequests.admin.*
import slinky.core.*
import slinky.core.WithAttrs.build
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import slinky.web.html.*
import tester.ui.components.{LoggedInUser, Notifications, UserSelector}
import tester.ui.requests.Request
import typings.antDesignIcons.components.AntdIcon
import typings.antd.components.*
import typings.antd.{antdInts, antdStrings}
import typings.csstype.mod.{OverflowYProperty, PositionProperty}
import typings.rcMenu.esInterfaceMod
import typings.react.mod.CSSProperties
import viewData.GroupDetailedInfoViewData

import scala.scalajs.js


object AdminBatchRegister {
  case class Props(loggedInUser: LoggedInUser)
  def apply(loggedInUser: LoggedInUser): ReactElement =
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(loggedInUser)))

  val component = FunctionalComponent[Props] { props =>

    val (userListCSV, setUserListCSV) = useState[String](s"ЛОГИН, ИМЯ, ФАМИЛИЯ, ПОЧТА, ПАРОЛЬ")

    val (parsedUsers, setParsedUsers) = useState[Seq[Option[RegistrationRequest]]](Seq())

    val (registrationResults, setRegistrationResults) = useState[Map[String, String]](Map())
    //    def reloadList(): Unit =
    //      Request.sendRequest(clientRequests.admin.GroupList, GroupListRequest(props.loggedInUser.token))(
    //        onComplete = {
    //          case GroupListResponseSuccess(groups) =>
    //            setGroups(groups)
    //          case GroupListResponseFailure() =>
    //            Notifications.showError(s"Не могу загрузить список групп (501)")
    //        }
    //      )

    useEffect(() => {

    }, Seq())


    def registerAll() = {
      import clientRequests.Registration
      for {
        uOpt <- parsedUsers
        u <- uOpt
      } {
        Request.sendRequest(Registration, u)(
          onComplete = r => setRegistrationResults(registrationResults + (u.login -> r.toString))
        )
      }
    }

    def updateCSV(csv: String) = {
      setUserListCSV(csv)
      setParsedUsers(
        csv
          .split("\n")
          .toSeq
          .map(s => s.split(",").toSeq.map(_.strip()) match {
            case Seq(login, firstName, lastName, email, password) =>
              Some(RegistrationRequest(
                login = login,
                password = password,
                firstName = firstName,
                lastName = lastName,
                email = email,
              ))

            case _ => None
          })
      )
    }

    import typings.antd.libTableInterfaceMod.{ColumnGroupType, ColumnType}
    val textEditor =
      TextArea
        .rows(5)
        .value(userListCSV)
        .onChange(e => updateCSV(e.target_ChangeEvent.value))
    import typings.antd.antdBooleans
    val table = section(
      Table[Option[RegistrationRequest]]()
        .bordered(true)
        .pagination(antdBooleans.`false`)
        .dataSourceVarargs(parsedUsers: _ *)
        .columnsVarargs(
          ColumnType[Option[RegistrationRequest]]()
            .setTitle("login")
            .setDataIndex("login ")
            .setKey("login")
            .setRender((_, tableItem, _) => build(p(tableItem.map(_.login).getOrElse("Не парсится")))),
          ColumnType[Option[RegistrationRequest]]()
            .setTitle("Имя")
            .setDataIndex("firstsName")
            .setKey("firstName")
            .setRender((_, tableItem, _) => build(p(tableItem.map(_.firstName).getOrElse("Не парсится")))),
          ColumnType[Option[RegistrationRequest]]()
            .setTitle("Фамилия")
            .setDataIndex("lastName")
            .setKey("lastName")
            .setRender((_, tableItem, _) => build(p(tableItem.map(_.lastName).getOrElse("Не парсится")))),
          ColumnType[Option[RegistrationRequest]]()
            .setTitle("Почта")
            .setDataIndex("email")
            .setKey("email")
            .setRender((_, tableItem, _) => build(p(tableItem.map(_.email).getOrElse("Не парсится")))),
          ColumnType[Option[RegistrationRequest]]()
            .setTitle("Пароль")
            .setDataIndex("password")
            .setKey("password")
            .setRender((_, tableItem, _) => build(p(tableItem.map(_.password).getOrElse("Не парсится")))),
          ColumnType[Option[RegistrationRequest]]()
            .setTitle("Результат")
            .setDataIndex("result")
            .setKey("result")
            .setRender((_, tableItem, _) => build(p(tableItem.map(r => registrationResults.get(r.login).getOrElse("Нет результата")).getOrElse("Не парсится")))),
        )
    )

    Card.bordered(true)
      .style(CSSProperties())(
        textEditor,
        table,
        Button.`type`(antdStrings.primary)
          .onClick(_ => registerAll())("Зарегистириовать")
      )
  }
}



