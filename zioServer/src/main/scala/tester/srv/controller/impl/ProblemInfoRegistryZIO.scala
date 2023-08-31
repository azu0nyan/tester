package tester.srv.controller.impl

import otsbridge.ProblemInfo
import tester.srv.controller.ProblemInfoRegistry
import zio.*
import zio.concurrent.ConcurrentMap

case class ProblemInfoRegistryZIO(map: ConcurrentMap[String, ProblemInfo]) extends ProblemInfoRegistry[UIO] {
  override def problemInfo(alias: String): UIO[Option[ProblemInfo]] =
    map.get(alias)

  override def registerProblemInfo(info: ProblemInfo): UIO[Unit] =
    map.put(info.uniqueAlias, info).map(_ => ())
}

object ProblemInfoRegistryZIO {
  def live: UIO[ProblemInfoRegistryZIO] =
    for {
      map <- ConcurrentMap.make[String, ProblemInfo]()
    } yield ProblemInfoRegistryZIO(map)
    
  def layer: ULayer[ProblemInfoRegistryZIO] = ZLayer.fromZIO(live)  
}