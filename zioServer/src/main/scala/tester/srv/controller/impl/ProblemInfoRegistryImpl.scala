package tester.srv.controller.impl

import otsbridge.ProblemInfo
import tester.srv.controller.ProblemInfoRegistry
import utils.safe
import zio.*
import zio.concurrent.ConcurrentMap

case class ProblemInfoRegistryImpl(map: ConcurrentMap[String, ProblemInfo]) extends ProblemInfoRegistry {
  override def problemInfo(alias: String): UIO[Option[ProblemInfo]] =
    map.get(alias)

  override def registerProblemInfo(info: ProblemInfo): UIO[Unit] =
    for {
      _ <- ZIO.log(s"Registering problem info ${info.alias} - ${safe(info.title(0))}")
      _ <- map.put(info.alias, info)
    } yield ()

  override def removeProblemInfo(alias: String): UIO[Unit] = map.remove(alias).map(_ => ())
  
  override def allInfos: UIO[Seq[ProblemInfo]] = map.toList.map(_.map(_._2))
}

object ProblemInfoRegistryImpl {
  def live: UIO[ProblemInfoRegistry] =
    for {
      map <- ConcurrentMap.make[String, ProblemInfo]()
    } yield ProblemInfoRegistryImpl(map)

  def layer: ULayer[ProblemInfoRegistry] = ZLayer.fromZIO(live)
}