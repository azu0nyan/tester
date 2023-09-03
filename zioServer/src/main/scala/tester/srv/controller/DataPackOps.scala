package tester.srv.controller

import otsbridge.DataPack
import tester.srv.controller.impl.ApplicationImpl
import zio.*

object DataPackOps {
  //todo use Application trait
  def registerInAppSingle(app: ApplicationImpl)(dp: DataPack): UIO[Unit] =
    for{
      _ <- ZIO.foreach(dp.courses)(c => app.courseTemplates.registerTemplate(c))
      _ <- ZIO.foreach(dp.problems)(c => app.problems.registerInfo(c))
      _ <- ZIO.foreach(dp.problems)(pt => app.verification.registerVerificator(pt.alias, AnswerVerificator.fromProblemTemplate(pt)))
    } yield ()

  def registerInApp(app: ApplicationImpl)(dps: DataPack*): UIO[Unit] =
    for {
      _ <- ZIO.foreach(dps)(dp => registerInAppSingle(app)(dp))
    } yield ()
}
