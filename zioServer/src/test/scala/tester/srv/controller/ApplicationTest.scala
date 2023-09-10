package tester.srv.controller

import tester.srv.controller.impl.ApplicationImpl
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*
import EmbeddedPG.EmbeddedPG

/*
object ApplicationTest  extends ZIOSpecDefault {
  def spec = suite("ApplicationTest")(

  ).provideSomeLayer(EmbeddedPG.databaseLayer)
    .provideSomeLayer(ApplicationImpl.layer) @@
    timeout(60.seconds) @@
    withLiveClock



}
*/