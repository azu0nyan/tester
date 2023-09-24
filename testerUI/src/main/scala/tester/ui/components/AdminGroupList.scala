package tester.ui.components

import clientRequests.admin.{AddUserToGroupFailure, AddUserToGroupRequest, AddUserToGroupSuccess, GroupListRequest, GroupListResponseFailure, GroupListResponseSuccess, RemoveUserFromGroupFailure, RemoveUserFromGroupRequest, RemoveUserFromGroupSuccess}

import scala.scalajs.js
import slinky.core.*
import slinky.web.html.*
import typings.antd.components.*
import typings.antd.{antdInts, antdStrings}
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import tester.ui.requests.Request
import typings.antDesignIcons.components.AntdIcon
import typings.csstype.mod.{OverflowYProperty, PositionProperty}
import typings.rcMenu.esInterfaceMod
import typings.react.mod.CSSProperties
import viewData.GroupDetailedInfoViewData
import slinky.core.WithAttrs.build


object AdminGroupList {
  case class Props(loggedInUser: LoggedInUser)
  def apply(loggedInUser: LoggedInUser): ReactElement =
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(loggedInUser)))

  val component = FunctionalComponent[Props] { props =>

    val (groups, setGroups) = useState[Seq[GroupDetailedInfoViewData]](Seq())


    def reloadList(): Unit =
      Request.sendRequest(clientRequests.admin.GroupList, GroupListRequest(props.loggedInUser.token))(
        onComplete = {
          case GroupListResponseSuccess(groups) =>
            setGroups(groups)
          case GroupListResponseFailure() =>
            Notifications.showError(s"Не могу загрузить список групп (501)")
        }
      )

    useEffect(() => {
      reloadList()
    }, Seq())

    def toTableItem(data: GroupDetailedInfoViewData): GroupTableItem =
      GroupTableItem(data.groupId, data.groupTitle, data.description, data.courses, data.users)


    case class GroupTableItem(id: String, title: String, description: String, courses: Seq[viewData.ShortCourseTemplateViewData], users: Seq[viewData.UserViewData])

    def addUserCell(groupId: String) = {
      val addUsers = UserSelector(props.loggedInUser, user => {
        import clientRequests.admin.AddUserToGroup
        Request.sendRequest(AddUserToGroup, AddUserToGroupRequest(props.loggedInUser.token, user.id, groupId))(
          onComplete = {
            case AddUserToGroupSuccess() =>
              reloadList()
            case AddUserToGroupFailure() =>
              Notifications.showError(s"Не могу добавить пользователя (501)")
          }
        )
      }, "Добавить")
      val removeUsers = UserSelector(props.loggedInUser, user => {
        import clientRequests.admin.RemoveUserFromGroup
        Request.sendRequest(RemoveUserFromGroup, RemoveUserFromGroupRequest(props.loggedInUser.token, user.id, groupId, true))(
          onComplete = {
            case RemoveUserFromGroupSuccess() =>
              reloadList()
            case RemoveUserFromGroupFailure() =>
              Notifications.showError(s"Не могу удалить пользователя (501)")
          }
        )
      }, "Удалить")
      div(addUsers, removeUsers)
    }


    if (groups.isEmpty)
      div(s"Загрузка списка групп...")
    else {
      import typings.antd.libTableInterfaceMod.{ColumnGroupType, ColumnType}
      section(
        Table[GroupTableItem]()
          .bordered(true)
          //        .dataSourceVarargs(toTableItem(a.head, 1))
          .dataSourceVarargs(groups.map(toTableItem): _ *)
          .columnsVarargs(
            ColumnType[GroupTableItem]()
              .setTitle("ID")
              .setDataIndex("id ")
              .setKey("id")
              .setRender((_, tableItem, _) => build(p(tableItem.id))),
            ColumnType[GroupTableItem]()
              .setTitle("Название")
              .setDataIndex("title")
              .setKey("title")
              .setRender((_, tableItem, _) => build(h5(tableItem.title))),
            ColumnType[GroupTableItem]()
              .setTitle("Описание")
              .setDataIndex("description")
              .setKey("description")
              .setRender((_, tableItem, _) => build(p(tableItem.description))),
            ColumnType[GroupTableItem]()
              .setTitle("Курсы")
              .setDataIndex("courses")
              .setKey("courses")
              .setRender((_, tableItem, _) => build(p(tableItem.courses.map(_.title).mkString(", ")))),
            ColumnType[GroupTableItem]()
              .setTitle("Ученики")
              .setDataIndex("users")
              .setKey("users")
              .setRender((_, tableItem, _) => build(p(tableItem.users.map(_.login).mkString(", ")))),
            ColumnType[GroupTableItem]()
              .setTitle("Добавить")
              .setDataIndex("users")
              .setKey("users")
              .setRender((_, tableItem, _) => addUserCell(tableItem.id)),
          )
      )
    }
  }
}


