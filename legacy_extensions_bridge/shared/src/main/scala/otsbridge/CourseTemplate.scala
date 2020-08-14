package otsbridge


trait CourseTemplate {

  val allowedForAll: Boolean = false

  val allowedInstances: Option[Int] = None

  def description: Option[String] = None

  // registerProblemListTemplate(this)

  val uniqueTemplates: Seq[ProblemTemplate]

  val courseTitle: String = "No title"

  val uniqueAlias: String

  case class GeneratedProblem(template: ProblemTemplate, seed: Int, attempts: Option[Int], initialScore: ProblemScore)

  type courseGeneratorOutput = Seq[GeneratedProblem]

  def generate(seed: Int): courseGeneratorOutput =
    uniqueTemplates.zipWithIndex.map { case (pt, i) => GeneratedProblem(pt, seed + i, pt.allowedAttempts, pt.initialScore) }.toSeq


  val timeLimitSeconds: Option[Int] = None

  //def grade


}



