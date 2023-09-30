package tester.srv.controller

import otsbridge.{ProblemInfo, ProblemTemplate}
import zio.UIO

trait ProblemInfoRegistry {
  def problemInfo(alias: String): UIO[Option[ProblemInfo]]
  def registerProblemInfo(info: ProblemInfo): UIO[Unit]
  def removeProblemInfo(alias: String): UIO[Unit]
  def allInfos: UIO[Seq[ProblemInfo]]
}
