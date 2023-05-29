package otsbridge

import otsbridge.ProblemScore._

object CompareProblemScore {

  def bestOf(s1:ProblemScore, s2:ProblemScore):ProblemScore = (s1, s2) match {
    case (BinaryScore(p1), BinaryScore(p2)) => if (p1 && !p2) s1 else s2
    case (DoubleScore(p1, _), DoubleScore(p2, _)) => if (p1> p2) s1 else s2
    case (XOutOfYScore(p1, _), XOutOfYScore(p2, _)) => if (p1> p2) s1 else s2
    case (m1@MultipleRunsResultScore(_), m2@MultipleRunsResultScore(_)) => if (m1.successesTotal> m2.successesTotal) s1 else s2
    case _ => if (s1.toInt > s2.toInt) s1 else s2
  }
}
