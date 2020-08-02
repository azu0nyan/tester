package otsbridge

object ProblemScore{

  def bestOf(s1:ProblemScore, s2:ProblemScore):ProblemScore = (s1, s2) match {
    case (BinaryScore(p1), BinaryScore(p2)) => if (p1 && !p2) s1 else s2
    case (IntScore(p1), IntScore(p2)) => if (p1> p2) s1 else s2
    case (DoubleScore(p1), DoubleScore(p2)) => if (p1> p2) s1 else s2
    case (XOutOfYScore(p1, _), XOutOfYScore(p2, _)) => if (p1> p2) s1 else s2
    case _ => s1
  }
}

sealed trait ProblemScore
case class BinaryScore(passed: Boolean) extends ProblemScore
case class IntScore(score: Int) extends ProblemScore
case class DoubleScore(score: Int) extends ProblemScore
case class XOutOfYScore(score: Int, maxScore: Int) extends ProblemScore

