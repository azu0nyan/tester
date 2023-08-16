package tester.srv.controller

import EmbeddedPG.EmbeddedPG
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object CourseTest extends ZIOSpecDefault {
  def spec = suite("UserOps test")(

  ).provideLayer(EmbeddedPG.connectionLayer) @@
    timeout(60.seconds) @@
    withLiveClock


}
