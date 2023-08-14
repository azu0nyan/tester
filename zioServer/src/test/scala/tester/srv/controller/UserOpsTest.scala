package tester.srv.controller

import EmbeddedPG.EmbeddedPG
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object UserOpsTest extends ZIOSpecDefault {
  def spec = suite("UserOps test")(
    checkFreeLogin
  ).provideLayer(EmbeddedPG.connectionLayer) @@
    timeout(60.seconds) @@
    withLiveClock

  val checkFreeLogin = test("Check free login") {
    val tt = UserOps.loginExists("nonExistentLogin").exit
    assertZIO(tt)(succeeds(equalTo(false)))
  }

}
