package frontend.views

import clientRequests.admin.{AddCourseToGroupRequest, AddUserToGroupRequest, ByNameOrLoginOrEmailMatch, GroupInfoRequest, GroupInfoResponseSuccess, RemoveUserFromGroup, RemoveUserFromGroupRequest, UserList, UserListRequest, UserListResponseFailure, UserListResponseSuccess}
import frontend._
import frontend.views.elements.TextFieldWithAutocomplete
import io.udash.core.ContainerView
import io.udash._
import org.scalajs.dom.html.Table
import org.scalajs.dom.{Element, Event}
import scalatags.JsDom
import scalatags.JsDom.all._
import scalatags.generic.Modifier
import viewData.GroupDetailedInfoViewData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.impl.Promise
import scala.util.Success

class AdminGroupInfoPageView(
                              presenter: AdminGroupInfoPagePresenter,
                              groupInfo: ModelProperty[viewData.GroupDetailedInfoViewData],
                            ) extends ContainerView {


  override def getTemplate: Modifier[Element] = produce(groupInfo)(groupInfo => div(
    h2(groupInfo.groupTitle),
    h3(s"id: ${groupInfo.groupId}"),
    p(s"${groupInfo.description}"),
    for (c <- groupInfo.courses) yield
      button(onclick :+= ((_: Event) => {
        presenter.app.goTo(AdminCourseTemplateInfoPageState(c.courseTemplateAlias))
        true // prevent default
      }))(c.title),
    div(styles.Custom.inputContainer ~)(
      label(`for` := "addUserId")("Добавить в группу:"),
      TextFieldWithAutocomplete(presenter.loginToAdd, Requests.reqUserSuggestion, "addUserIdVars"),
      //TextInput(presenter.loginToAdd)(id := "addUserId", placeholder := "Логин или ИД"),
      button(styles.Custom.primaryButton ~, onclick :+= ((_: Event) => {
        presenter.addUser()
        true // prevent default
      }))("Добавить"),
    ),
    div(styles.Custom.inputContainer ~)(
      label(`for` := "removeUserId")("Удалить из группы:"),
      TextFieldWithAutocomplete(presenter.loginToRemove, Requests.reqUserSuggestion, "removeUserIdVars"),
      label(`for` := "forceCourseRemovalId")("Удалить все курсы"),
      Checkbox(presenter.forceCourseRemoval)(id := "forceCourseRemovalId"),
      button(styles.Custom.primaryButton ~, onclick :+= ((_: Event) => {
        presenter.removeUser()
        true // prevent default
      }))("Удалить"),
    ),
    div(styles.Custom.inputContainer ~)(
      label(`for` := "addCourseId")("Добавить курс:"),
      TextInput(presenter.courseToAdd)(id := "addCourseId", placeholder := "Алиас"),
      button(styles.Custom.primaryButton ~, onclick :+= ((_: Event) => {
        presenter.addCourse()
        true // prevent default
      }))("Добавить"),
    ),
    userTable(groupInfo.users)
  ).render
  )

  def userTable(users: Seq[viewData.UserViewData]): JsDom.TypedTag[Table] = table(styles.Custom.defaultTable ~)(
    tr(
      th(width := "150px")("Логин"),
      th(width := "150px")("Имя"),
      th(width := "150px")("Фамилия"),
      th(width := "150px")("EMAIL"),
    ),
    for (u <- users.sortBy(_.login)) yield tr(
      td(u.login),
      td(u.firstName.getOrElse("").toString),
      td(u.lastName.getOrElse("").toString),
      td(u.email.getOrElse("").toString),
    )
  )
}

case class AdminGroupInfoPagePresenter(
                                        groupInfo: ModelProperty[viewData.GroupDetailedInfoViewData],

                                        app: Application[RoutingState]) extends GenericPresenter[AdminGroupInfoPageState] {
  val loginToAdd: Property[String] = Property.blank[String]
  val loginToRemove: Property[String] = Property.blank[String]
  val courseToAdd: Property[String] = Property.blank[String]
  val forceCourseRemoval: Property[Boolean] = Property.blank[Boolean]




  def addCourse() = {
    frontend.sendRequest(clientRequests.admin.AddCourseToGroup, AddCourseToGroupRequest(currentToken.get, courseToAdd.get, groupInfo.get.groupId, true)) onComplete {
      case Success(_) => Requests.requestGroupInfoUpdate(groupInfo.get.groupId, groupInfo)
      case resp@_ =>
        if (debugAlerts) showErrorAlert(s"$resp")
    }
  }

  def removeUser() = {
    frontend.sendRequest(clientRequests.admin.RemoveUserFromGroup,
      RemoveUserFromGroupRequest(currentToken.get, loginToRemove.get, groupInfo.get.groupId, forceCourseRemoval.get)) onComplete {
      case Success(_) => Requests.requestGroupInfoUpdate(groupInfo.get.groupId, groupInfo)
      case resp@_ =>
        if (debugAlerts) showErrorAlert(s"$resp")
    }
  }

  def addUser() = {
    Requests.addUser(loginToAdd.get.split(" ").headOption.getOrElse(""), groupInfo.get.groupId) onComplete {
      case Success(_) => Requests.requestGroupInfoUpdate(groupInfo.get.groupId, groupInfo)
      case resp@_ =>
        if (debugAlerts) showErrorAlert(s"$resp")
    }
  }




  override def handleState(state: AdminGroupInfoPageState): Unit = {
    println(s"Admin groups info page handling state")
    Requests.requestGroupInfoUpdate(state.groupId, groupInfo)
  }

}

case object AdminGroupInfoPageViewFactory extends ViewFactory[AdminGroupInfoPageState] {
  override def create(): (View, Presenter[AdminGroupInfoPageState]) = {
    println(s"Admin group info page view factory creating..")
    val model = ModelProperty.blank[viewData.GroupDetailedInfoViewData]
    val presenter = AdminGroupInfoPagePresenter(model,
      frontend.applicationInstance)
    val view = new AdminGroupInfoPageView(presenter, model)
    (view, presenter)
  }
}