package tester.srv.controller.impl

import tester.srv.controller.{AnswerVerificator, AnswerVerificatorRegistry}
import zio.*
import zio.concurrent.ConcurrentMap

class AnswerVerificatorRegistryImpl(map: ConcurrentMap[String, AnswerVerificator]) extends AnswerVerificatorRegistry {
  override def getVerificator(alias: String): UIO[Option[AnswerVerificator]] =
    map.get(alias)

  override def registerVerificator(alias: String, info: AnswerVerificator): UIO[Unit] =
    map.put(alias, info).map(_ => ())
}

object AnswerVerificatorRegistryImpl {
  def live: UIO[AnswerVerificatorRegistry] =
    for {
      map <- ConcurrentMap.make[String, AnswerVerificator]()
    } yield AnswerVerificatorRegistryImpl(map)

  def layer: ULayer[AnswerVerificatorRegistry] = ZLayer.fromZIO(live)

}
