package otsbridge

import otsbridge.ProgramRunResult._

object ProblemScore {
  sealed trait ProblemScore{
    def toPrettyString:String

    def toInt:Int
  }
  case class BinaryScore(passed: Boolean) extends ProblemScore {
    override def toInt: Int = if(passed) 1 else 0
    override def toPrettyString: String = passed.toString
  }
  case class IntScore(score: Int) extends ProblemScore {
    override def toInt: Int = score
    override def toPrettyString: String = score.toString
  }
  case class DoubleScore(score: Double, maxScore: Double) extends ProblemScore {
    override def toInt: Int =  math.ceil(score).toInt
    override def toPrettyString: String = s"${score.toString} / ${maxScore.toString}"
  }
  case class XOutOfYScore(score: Int, maxScore: Int) extends ProblemScore {
    override def toInt: Int = score
    override def toPrettyString: String = s"${score} / ${maxScore.toString}"
  }
  case class MultipleRunsResultScore(runResults: Seq[ProgramRunResult]) extends ProblemScore {

    def successesTotal: Int = runResults.count(_.isInstanceOf[ProgramRunResultSuccess])
    def tasksTotal: Int = runResults.size
    //  def toXOutOfY:XOutOfYScore = XOutOfYScore(successesTotal, tasksTotal)
    override def toInt: Int = successesTotal
    override def toPrettyString: String = s"${successesTotal} / ${tasksTotal.toString}"
  }
}


