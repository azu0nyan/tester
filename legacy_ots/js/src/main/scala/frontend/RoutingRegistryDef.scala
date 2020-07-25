package frontend

import io.udash._
import io.udash.core.Url

class RoutingRegistryDef extends RoutingRegistry[RoutingState]{
  override def matchUrl(url: Url): RoutingState =  {
    val res = url2State.applyOrElse(
      url.value.stripSuffix("/"),
      (x: String) => LandingPageState
    )
    println(s"url matched : $url ->  $res")
    res
  }

  override def matchState(state: RoutingState): Url =  {
    val res = Url(state2Url.apply(state))
    println(s"state matched : $state -> $res ")
    res
  }

  private val (url2State, state2Url) = bidirectional {
    case "/" => LandingPageState
    case "/login" => LoginPageState
    case "/register" => RegistrationPageState
    case "/courseSelection"  => CourseSelectionPageState
    case "/course" / id => CoursePageState(id, "")
//    case "/course" / id  =>  CoursePageState(id, taskId )
    case "/app" => AppPageState
  }

}
