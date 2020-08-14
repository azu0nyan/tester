package otsbridge

object ProblemScore{

  def bestOf(s1:ProblemScore, s2:ProblemScore):ProblemScore = (s1, s2) match {
    case (BinaryScore(p1), BinaryScore(p2)) => if (p1 && !p2) s1 else s2
    case (IntScore(p1), IntScore(p2)) => if (p1> p2) s1 else s2
    case (DoubleScore(p1), DoubleScore(p2)) => if (p1> p2) s1 else s2
    case (XOutOfYScore(p1, _), XOutOfYScore(p2, _)) => if (p1> p2) s1 else s2
    case (m1@MultipleRunsResultScore(_), m2@MultipleRunsResultScore(_)) => if (m1.successesTotal> m2.successesTotal) s1 else s2
    case _ => s1
  }
}



sealed trait ProblemScore
case class BinaryScore(passed: Boolean) extends ProblemScore
case class IntScore(score: Int) extends ProblemScore
case class DoubleScore(score: Int) extends ProblemScore
case class XOutOfYScore(score: Int, maxScore: Int) extends ProblemScore
case class MultipleRunsResultScore(runResults:Seq[ProgramRunResult]) extends ProblemScore {
  def successesTotal :Int = runResults.count(_.success)
  def tasksTotal:Int  = runResults.count(_.success)
  def toXOutOfY:XOutOfYScore = XOutOfYScore(successesTotal, tasksTotal)
}

sealed trait ProgramRunResult{
  val success:Boolean
}

case class ProgramRunResultSuccess(timeMS: Long, message:Option[String]) extends ProgramRunResult {
  override val success:Boolean = true
}

case class ProgramRunResultWrongAnswer(timeMS: Long, message:Option[String]) extends ProgramRunResult {
  override val success:Boolean = false
}

case class ProgramRunResultFailure(message:Option[String]) extends ProgramRunResult {
  override val success: Boolean = false
}



