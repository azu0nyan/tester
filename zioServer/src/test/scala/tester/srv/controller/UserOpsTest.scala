package tester.srv.controller

import EmbeddedPG.EmbeddedPG
import tester.srv.controller.UserOps.{RegistrationData, RegistrationResult}
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object UserOpsTest extends ZIOSpecDefault {
  def spec = suite("UserOps test")(
    checkFreeLogin,
    registration
  ).provideLayer(EmbeddedPG.connectionLayer) @@
    timeout(60.seconds) @@
    withLiveClock

  val checkFreeLogin = test("Check free login") {
    val tt = UserOps.loginExists("nonExistentLogin").exit
    assertZIO(tt)(succeeds(equalTo(false)))
  }

  val registration = test("Registration test") {
    val req = RegistrationData("regUserLogin", "password", "Aliecbob", "Joens", "a@a.com" )
    for {
      notExists <- UserOps.loginExists("regUserLogin")
      regResult <- UserOps.registerUser(req)
      exists <- UserOps.loginExists("regUserLogin")
      data <- UserOps.getUser("regUserLogin")
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

}
