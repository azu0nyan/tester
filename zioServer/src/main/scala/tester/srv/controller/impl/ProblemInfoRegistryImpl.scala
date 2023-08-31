package tester.srv.controller.impl

import otsbridge.ProblemInfo
import tester.srv.controller.ProblemInfoRegistry
import zio.*
import zio.concurrent.ConcurrentMap

case class ProblemInfoRegistryImpl(map: ConcurrentMap[String, ProblemInfo]) extends ProblemInfoRegistry {
  override def problemInfo(alias: String): UIO[Option[ProblemInfo]] =
    map.get(alias)

  override def registerProblemInfo(info: ProblemInfo): UIO[Unit] =
    map.put(info.uniqueAlias, info).map(_ => ())
}

object ProblemInfoRegistryImpl {
  def live: UIO[ProblemInfoRegistryImpl] =
    for {
      map <- ConcurrentMap.make[String, ProblemInfo]()
    } yield ProblemInfoRegistryImpl(map)
    
  def layer: ULayer[ProblemInfoRegistryImpl] = ZLayer.fromZIO(live)  
}