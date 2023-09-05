package tester.srv.controller.impl

import otsbridge.ProblemInfo
import tester.srv.controller.ProblemInfoRegistry
import zio.*
import zio.concurrent.ConcurrentMap

case class ProblemInfoRegistryImpl(map: ConcurrentMap[String, ProblemInfo]) extends ProblemInfoRegistry {
  override def problemInfo(alias: String): UIO[Option[ProblemInfo]] =
    map.get(alias)

  override def registerProblemInfo(info: ProblemInfo): UIO[Unit] =
    for {
      _ <- ZIO.log(s"Registering problem info ${info.alias} ${
        try {
          info.title(0)
        } catch
          case t: Throwable => t.toString
      }")
      _ <- map.put(info.alias, info)
    } yield ()
}

object ProblemInfoRegistryImpl {
  def live: UIO[ProblemInfoRegistry] =
    for {
      map <- ConcurrentMap.make[String, ProblemInfo]()
    } yield ProblemInfoRegistryImpl(map)

  def layer: ULayer[ProblemInfoRegistry] = ZLayer.fromZIO(live)
}