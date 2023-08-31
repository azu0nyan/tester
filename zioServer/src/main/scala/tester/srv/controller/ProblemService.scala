package tester.srv.controller

import otsbridge.ProblemScore.ProblemScore
import zio.*

trait ProblemService[F[_]: TagK] {

  /** Returns problem id */
  def startProblem(courseId: Int, templateAlias: String): F[Int]

  def removeProblem(courseId: Int, templateAlias: String): F[Boolean]
  
  def answerConfirmed(problemId: Int, asnwerId:Int, score: ProblemScore): F[Unit]
}
