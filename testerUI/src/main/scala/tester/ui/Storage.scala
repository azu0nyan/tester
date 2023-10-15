package tester.ui

object Storage {

  import org.scalajs.dom.window.sessionStorage
  import org.scalajs.dom.window.localStorage

  private def setSession(key: String, value: String): Unit =
    sessionStorage.setItem(key, value)

  private def getSession(key: String): Option[String] =
    Option(sessionStorage.getItem(key))

  private def setLocal(key: String, value: String): Unit =
    localStorage.setItem(key, value)

  private def getLocal(key: String): Option[String] =
    Option(localStorage.getItem(key))

  private def setLocalPersonal(key: String, value: String): Unit =
    setLocal(getUserLogin.getOrElse("") + "_" + key, value)

  private def getLocalPersonal(key: String): Option[String] =
    getLocal(getUserLogin.getOrElse("") + "_" + key)


  def setUserLogin = setSession("user_login", _)
  def getUserLogin = getSession("user_login")

  def setUserToken = setSession("user_token", _)
  def getUserToken = getSession("user_token")

  def setTheme = setLocalPersonal("ace_theme", _)
  def getTheme = getLocalPersonal("ace_theme").getOrElse("github")

  def setFontSize(s: Int) = setLocalPersonal("ace_font_size", s.toString)
  def getFontSize: Int = getLocalPersonal("ace_font_size").flatMap(_.toIntOption).getOrElse(14)

  def readUserAnswer(uniqueId: String): Option[String] = getSession(uniqueId)
  def setUserAnswer(uniqueId: String, answer: String): Unit = setSession(uniqueId, answer)

}
