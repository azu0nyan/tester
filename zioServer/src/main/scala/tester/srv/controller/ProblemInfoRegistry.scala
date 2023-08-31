package tester.srv.controller

import otsbridge.{ProblemInfo, ProblemTemplate}

trait ProblemInfoRegistry[F[_]] {
  def problemInfo(alias: String): F[Option[ProblemInfo]]
  def registerProblemInfo(info: ProblemInfo): F[Unit]
}
