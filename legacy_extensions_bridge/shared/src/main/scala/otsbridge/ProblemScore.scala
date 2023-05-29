package otsbridge

import otsbridge.ProgramRunResult._

object ProblemScore {
  sealed trait ProblemScore {
    def toPrettyString: String

    def toInt: Int

    def isMax: Boolean

    def percentage: Double
  }

  case class BinaryScore(passed: Boolean) extends ProblemScore {
    override def toInt: Int = if (passed) 1 else 0
    override def toPrettyString: String = passed.toString
    override def isMax: Boolean = passed

    override def percentage: Double = toInt.toDouble

  }

  case class DoubleScore(score: Double, maxScore: Double) extends ProblemScore {
    override def toInt: Int = math.ceil(score).toInt
    override def toPrettyString: String = s"${score.toString} / ${maxScore.toString}"
    override def isMax: Boolean = score == maxScore

    override def percentage: Double = score / maxScore
  }
  case class XOutOfYScore(score: Int, maxScore: Int) extends ProblemScore {
    override def toInt: Int = score
    override def toPrettyString: String = s"${score} / ${maxScore.toString}"
    override def isMax: Boolean = score == maxScore
    override def percentage: Double = score.toDouble / maxScore.toDouble
  }
  case class MultipleRunsResultScore(runResults: Seq[ProgramRunResult]) extends ProblemScore {

    def successesTotal: Int = runResults.count(_.isInstanceOf[ProgramRunResultSuccess])
    def tasksTotal: Int = runResults.size
    //  def toXOutOfY:XOutOfYScore = XOutOfYScore(successesTotal, tasksTotal)
    override def toInt: Int = successesTotal
    override def toPrettyString: String = s"${successesTotal} / ${tasksTotal.toString}"

    override def isMax: Boolean = successesTotal == tasksTotal

    override def percentage: Double = successesTotal.toDouble / tasksTotal.toDouble
  }
}


