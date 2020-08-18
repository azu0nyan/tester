package otsbridge

import otsbridge.ProgramRunResult._

object ProblemScore {
  sealed trait ProblemScore{
    def toInt:Int
  }
  case class BinaryScore(passed: Boolean) extends ProblemScore {
    override def toInt: Int = if(passed) 1 else 0
  }
  case class IntScore(score: Int) extends ProblemScore {
    override def toInt: Int = score
  }
  case class DoubleScore(score: Double, maxScore: Double) extends ProblemScore {
    override def toInt: Int =  math.ceil(score).toInt
  }
  case class XOutOfYScore(score: Int, maxScore: Int) extends ProblemScore {
    override def toInt: Int = score
  }
  case class MultipleRunsResultScore(runResults: Seq[ProgramRunResult]) extends ProblemScore {
    def successesTotal: Int = runResults.count(_.isInstanceOf[ProgramRunResultSuccess])
    def tasksTotal: Int = runResults.size
    //  def toXOutOfY:XOutOfYScore = XOutOfYScore(successesTotal, tasksTotal)
    override def toInt: Int = successesTotal
  }
}


