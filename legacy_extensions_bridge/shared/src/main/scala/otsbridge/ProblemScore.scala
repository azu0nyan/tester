package otsbridge

import otsbridge.ProgramRunResult._

object ProblemScore {
  sealed trait ProblemScore
  case class BinaryScore(passed: Boolean) extends ProblemScore
  case class IntScore(score: Int) extends ProblemScore
  case class DoubleScore(score: Int, maxScore: Int) extends ProblemScore
  case class XOutOfYScore(score: Int, maxScore: Int) extends ProblemScore
  case class MultipleRunsResultScore(runResults: Seq[ProgramRunResult]) extends ProblemScore {
    def successesTotal: Int = runResults.count(_.isInstanceOf[ProgramRunResultSuccess])
    def tasksTotal: Int = runResults.size
    //  def toXOutOfY:XOutOfYScore = XOutOfYScore(successesTotal, tasksTotal)
  }
}


