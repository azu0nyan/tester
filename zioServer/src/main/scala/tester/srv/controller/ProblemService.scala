package tester.srv.controller

trait ProblemService[F[_]] {

  /** Returns problem id */
  def startProblem(courseId: Int, templateAlias: String): F[Int]

  def removeProblem(courseId: Int, templateAlias: String): F[Unit]
}
