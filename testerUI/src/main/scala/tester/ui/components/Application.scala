package tester.ui.components

import clientRequests.*
import slinky.core.*
import slinky.web.ReactDOM
import slinky.web.html.*
import tester.ui.Storage
//import org.scalajs.dom.*
import slinky.core.facade.Hooks.useState
import slinky.core.facade.ReactElement
import tester.ui.components.admin.AdminAppLayout
import tester.ui.requests.Request.sendRequest
import typings.betterReactMathjax.components.MathJaxContext
import typings.betterReactMathjax.mathJaxContextMathJaxContextMod.MathJaxContextProps
import typings.betterReactMathjax.mathJaxContextMod

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import viewData.*

import java.time.Instant

@JSImport("antd/dist/antd.css", JSImport.Default)
@js.native
object CSS extends js.Any

sealed trait UserAppData
case class LoggedInUser(token: String, userViewData: UserViewData, isTeacher: Boolean, isAdmin: Boolean) extends UserAppData
case class NoUser() extends UserAppData

object Application {
  case class Props()

  def apply(): ReactElement = {
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props()))
  }
  private val css = CSS

  sealed trait ApplicationState
  case object StudentAppState extends ApplicationState
  case object TeacherAppState extends ApplicationState
//  case object WatcherAppState extends ApplicationState
  case object AdminAppState extends ApplicationState



  val component = FunctionalComponent[Props] { props =>
    val (loggedInUser, setLoggedInUser) = useState[UserAppData](NoUser())
    val (appState, setAppState) = useState[ApplicationState](StudentAppState)


    def tryLogin(lp: LoginForm.LoginPassword): Unit = {
      sendRequest(Login, LoginRequest(lp.login, lp.password))(onComplete = {
        case LoginSuccessResponse(token, userData, isTeacher, isAdmin ) =>
          setLoggedInUser(LoggedInUser(token, userData, isTeacher, isAdmin))
          Storage.setUserToken(token)
          Storage.setUserLogin(userData.login)
        case LoginFailureUserNotFoundResponse() =>
          Notifications.showError(s"Неизвестный логин")
        case LoginFailureWrongPasswordResponse() =>
          Notifications.showError(s"Неизвестный пароль")
        case LoginFailureUnknownErrorResponse() =>
          Notifications.showError(s"Ошибка 501")
      }, onFailure = x => {
        Notifications.showError(s"Ошибка 404")
        x.printStackTrace()
      })
    }
    //    useEffect(() => {})

    def logout() = {
      setLoggedInUser(NoUser())
      Storage.setUserToken("")
      Storage.setUserLogin("")
    }

    mathJaxContextMod.MathJaxContext.apply(MathJaxContextProps.configMathJax3Configundef())(
      div(
        //      MathJaxContext.configMathJax3Configundef.build,
        loggedInUser match {
          case l: LoggedInUser =>
            appState match {
              case StudentAppState => UserAppLayout(l, logout = logout, setAppState)
              case TeacherAppState => TeacherAppLayout(l, logout = logout, setAppState)

//                div("Teacher UI")
//              case WatcherAppState => div("Watcher UI")
              case AdminAppState => AdminAppLayout(l, logout = logout, setAppState)
            }
          case NoUser() => LoginForm( tryLogin)
        }

      ))
  }
}



