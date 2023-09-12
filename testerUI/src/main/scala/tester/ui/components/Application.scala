package tester.ui.components

import clientRequests._
import slinky.core._
import slinky.web.ReactDOM
import slinky.web.html._

import org.scalajs.dom._
import slinky.core.facade.Hooks.useState
import slinky.core.facade.ReactElement
import tester.ui.requests.Request.sendRequest
import typings.betterReactMathjax.components.MathJaxContext
import typings.betterReactMathjax.mathJaxContextMathJaxContextMod.MathJaxContextProps
import typings.betterReactMathjax.mathJaxContextMod

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import viewData._

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

    mathJaxContextMod.MathJaxContext.apply(MathJaxContextProps.configMathJax3Configundef())(
      div(
        //      MathJaxContext.configMathJax3Configundef.build,
        loggedInUser match {
          case l: LoggedInUser =>
            appState match {
              case StudentAppState => UserAppLayout(l, logout = () => setLoggedInUser(NoUser()), setAppState)
              case TeacherAppState => TeacherAppLayout(l, logout = () => setLoggedInUser(NoUser()))

//                div("Teacher UI")
//              case WatcherAppState => div("Watcher UI")
              case AdminAppState => AdminAppLayout(l, logout = () => setLoggedInUser(NoUser()))
            }
          case NoUser() => LoginForm( tryLogin)
        }

      ))
  }
}



