package frontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import io.udash._
import frontend._
import org.scalajs.dom._
import scalatags.JsDom.all._
import io.udash.core.ContainerView
import scalatags.generic.Modifier
import clientRequests.{LoginFailure, LoginFailureFrontendException, LoginFailureUnknownErrorResponse, LoginFailureUserNotFoundResponse, LoginFailureWrongPasswordResponse, LoginRequest, LoginSuccessResponse}
import viewData.UserViewData

import scala.util.{Failure, Success}

class LoginPageView(
                     model: ModelProperty[UserCredentialsData],
                     presenter: LoginPagePresenter) extends ContainerView {

  val loginId = "loginInput"
  val passwordId = "passwordInput"
  override def getTemplate: Modifier[Element] = div(styles.Grid.content ~)(div(styles.Custom.inputContainerPositioner ~)(
    form(styles.Custom.inputContainer ~)(
      label(`for` := loginId)("Логин:"),
      TextInput(model.subProp(_.login))(id := loginId, placeholder := "Логин..."),
      label(`for` := loginId)("Пароль:"),
      PasswordInput(model.subProp(_.password))(id := passwordId, placeholder := "Пароль..."),
      button(styles.Custom.primaryButton ~, onclick :+= ((_: Event) => {
        presenter.logIn()
        true // prevent default
      }))("Войти"),
      button(onclick :+= ((_: Event) => {
        presenter.toLandingPage()
        true // prevent default
      }))("Назад"),

      //      genButton("Войти", () => presenter.logIn()),
      //      genButton("Назад", () => presenter.toLandingPage()

    ),
  )
  )

}

case class LoginPagePresenter(
                               model: ModelProperty[UserCredentialsData],
                               app: Application[RoutingState]
                             ) extends GenericPresenter[LoginPageState.type] {
  def logIn(): Unit = {
    val login = model.subProp(_.login).get
    val pass = model.subProp(_.password).get
    println("logging in ...")

    frontend.sendRequest(clientRequests.Login, LoginRequest(login, pass)) onComplete {
      case Success(LoginSuccessResponse(token, userViewData)) => onLoginSuccess(token, userViewData)
      case Success(r: LoginFailure) => r match {
        case LoginFailureUserNotFoundResponse() | LoginFailureWrongPasswordResponse() =>
           showErrorAlert("Неверный логин или пароль")
        case LoginFailureUnknownErrorResponse() =>
          showErrorAlert()
      }
      case Failure(exception) =>
        showErrorAlert()
    }
  }

  def onLoginSuccess(token: Token, userViewData: UserViewData): Unit = {
    println(s"Login success $token $userViewData")
    setTokenCookie(token)
    currentToken.set(token, true)
    currentUser.set(Some(userViewData))
    app.goTo(CourseSelectionPageState)
  }



  override def handleState(state: LoginPageState.type): Unit = {
    println(s"Login page presenter,  handling state : $state")
  }

}

case object LoginPageViewFactory extends ViewFactory[LoginPageState.type] {
  override def create(): (View, Presenter[LoginPageState.type]) = {
    println(s"Login  page view factory creating..")
    //      println(extractHost(document.documentURI))
    val model = ModelProperty(UserCredentialsData("", ""))
    val presenter = new LoginPagePresenter(model, frontend.applicationInstance)
    val view = new LoginPageView(model, presenter)
    (view, presenter)
  }
}