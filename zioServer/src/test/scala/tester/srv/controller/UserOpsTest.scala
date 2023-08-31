package tester.srv.controller

import EmbeddedPG.EmbeddedPG
import tester.srv.controller.UserService.LoginResult.LoggedIn
import tester.srv.controller.UserService.RegistrationResult.AlreadyExists
import tester.srv.controller.UserService.{LoginData, RegistrationData, RegistrationResult}
import tester.srv.controller.impl.UserServiceImpl
import tester.srv.dao.{RegisteredUserDao, UserSessionDao}
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
    val tt = RegisteredUserDao.loginExists("nonExistentLogin").exit
    assertZIO(tt)(succeeds(equalTo(false)))
  }

  val registration = test("Registration test") {
    val req = RegistrationData("regUserLogin", "password", "Aliecbob", "Joens", "a@a.com")
    for {
      notExists <- RegisteredUserDao.loginExists("regUserLogin")
      regResult <- UserServiceImpl.registerUser(req)
      exists <- RegisteredUserDao.loginExists("regUserLogin")
      dataOpt <- RegisteredUserDao.byLogin("regUserLogin")
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
      _ <- UserServiceImpl.registerUser(data1)
      res <- UserServiceImpl.registerUser(data2)
    } yield assertTrue(res == AlreadyExists("double"))
  }

  val loginTest = test("Login test") {
    val data1 = RegistrationData("loginTester", "password", "Aliecbob", "Joens", "a@a.com")
    val loginData = LoginData("loginTester", "password")
    for {
      _ <- UserServiceImpl.registerUser(data1)
      usrOpt <- RegisteredUserDao.byLogin("loginTester")
      result <- UserServiceImpl.loginUser(loginData)
      sessions <- UserSessionDao.getValidUserSessions(usrOpt.get.id)
      loginResult <- UserServiceImpl.validateToken(result.asInstanceOf[LoggedIn].token)
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
      _ <- UserServiceImpl.registerUser(data1)
      usrOpt <- RegisteredUserDao.byLogin("loginTester")
      result <- UserServiceImpl.loginUser(loginData)
      token = result.asInstanceOf[LoggedIn].token
      loginResultBefore <- UserServiceImpl.validateToken(token)
      _ <- UserSessionDao.invalidateSessionByToken(token)
      loginResultAfter <- UserServiceImpl.validateToken(token)
      sessions <- UserSessionDao.getValidUserSessions(usrOpt.get.id)
    } yield assertTrue(
      loginResultBefore.isInstanceOf[TokenOps.TokenValid],
      loginResultAfter.isInstanceOf[TokenOps.InvalidToken.type],
      sessions.isEmpty
    )
  }

}
