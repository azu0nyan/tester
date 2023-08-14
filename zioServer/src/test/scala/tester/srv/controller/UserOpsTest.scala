package tester.srv.controller

import EmbeddedPG.EmbeddedPG
import tester.srv.controller.UserOps.LoginResult.LoggedIn
import tester.srv.controller.UserOps.RegistrationResult.AlreadyExists
import tester.srv.controller.UserOps.{LoginData, RegistrationData, RegistrationResult}
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object UserOpsTest extends ZIOSpecDefault {
  def spec = suite("UserOps test")(
    checkFreeLogin,
    registration,
    noDoubleRegistration,
    loginTest,
    logoutTest
  ).provideLayer(EmbeddedPG.connectionLayer) @@
    timeout(60.seconds) @@
    withLiveClock

  val checkFreeLogin = test("Check free login") {
    val tt = UserOps.loginExists("nonExistentLogin").exit
    assertZIO(tt)(succeeds(equalTo(false)))
  }

  val registration = test("Registration test") {
    val req = RegistrationData("regUserLogin", "password", "Aliecbob", "Joens", "a@a.com")
    for {
      notExists <- UserOps.loginExists("regUserLogin")
      regResult <- UserOps.registerUser(req)
      exists <- UserOps.loginExists("regUserLogin")
      dataOpt <- UserOps.getUser("regUserLogin")
      data = dataOpt.get
    } yield assertTrue(
      notExists == false,
      exists == true,
      regResult == RegistrationResult.Success,
      data.login == req.login,
      data.email == req.email,
      data.firstName == req.firstName,
      data.lastName == req.lastName,
      data.passwordHash != "",
      data.passwordSalt != "",
    )
  }

  val noDoubleRegistration = test("No double registration") {
    val data1 = RegistrationData("double", "password", "Aliecbob", "Joens", "a@a.com")
    val data2 = RegistrationData("double", "password", "Aliecbob2", "Joens2", "a@a.com")
    for {
      _ <- UserOps.registerUser(data1)
      res <- UserOps.registerUser(data2)
    } yield assertTrue(res == AlreadyExists("double"))
  }

  val loginTest = test("Login test") {
    val data1 = RegistrationData("loginTester", "password", "Aliecbob", "Joens", "a@a.com")
    val loginData = LoginData("loginTester", "password")
    for {
      _ <- UserOps.registerUser(data1)
      usrOpt <- UserOps.getUser("loginTester")
      result <- UserOps.loginUser(loginData)
      sessions <- UserOps.getValidUserSessions(usrOpt.get.id)
      loginResult <- UserOps.validateToken(result.asInstanceOf[LoggedIn].token)
    } yield assertTrue(
      loginResult.isInstanceOf[TokenOps.TokenValid],
      loginResult.asInstanceOf[TokenOps.TokenValid].id == usrOpt.get.id,
      sessions.size == 1,
      sessions.head.userId == usrOpt.get.id,
      sessions.head.token == result.asInstanceOf[LoggedIn].token
    )
  }

  val logoutTest = test("Logout test") {
    val data1 = RegistrationData("loginTester", "password", "Aliecbob", "Joens", "a@a.com")
    val loginData = LoginData("loginTester", "password")

    for {
      _ <- UserOps.registerUser(data1)
      usrOpt <- UserOps.getUser("loginTester")
      result <- UserOps.loginUser(loginData)
      token = result.asInstanceOf[LoggedIn].token
      loginResultBefore <- UserOps.validateToken(token)
      _ <- UserOps.invalidateSessionByToken(token)
      loginResultAfter <- UserOps.validateToken(token)
      sessions <- UserOps.getValidUserSessions(usrOpt.get.id)
    } yield assertTrue(
      loginResultBefore.isInstanceOf[TokenOps.TokenValid],
      loginResultAfter.isInstanceOf[TokenOps.InvalidToken.type],
      sessions.isEmpty
    )
  }

}
