package otsbridge

import otsbridge.CoursePiece.CourseMainPiece
import otsbridge.ProblemScore.ProblemScore

case class GeneratedProblem(template: ProblemTemplate, seed: Int, attempts: Option[Int], initialScore: ProblemScore)

trait CourseTemplate {

  val allowedForAll: Boolean = false

  val allowedInstances: Option[Int] = None

  def description: Option[String] = None

  // registerProblemListTemplate(this)

  def problemsToGenerate: Seq[ProblemTemplate]

  def courseData:CourseMainPiece

  val courseTitle: String

  val uniqueAlias: String

  type CourseGeneratorOutput = Seq[GeneratedProblem]

  def generate(seed: Int): CourseGeneratorOutput =
    problemsToGenerate.zipWithIndex.map { case (pt, i) => pt.generate(seed + i)}.toSeq


  val timeLimitSeconds: Option[Int] = None

  //def grade


}



