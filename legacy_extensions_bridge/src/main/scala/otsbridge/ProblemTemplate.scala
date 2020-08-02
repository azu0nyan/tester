package otsbridge


import scala.concurrent.Future
object ProblemTemplate{
  type ProblemTemplateAlias = String
}


trait ProblemTemplate {
  val initialScore: ProblemScore
  val uniqueAlias: String
  val allowedAttempts: Int = 1
  //def answerFromString[AT](field: AnswerFieldType[AT]): Option[AT] = ???
  //def generateProblem(seed: Int): ProblemInstance = ProblemInstance.create()
  def generateProblemHtml(seed: Int): String
  def answerField(seed: Int): AnswerField
  def submitAnswer(seed: Int, answer: String, onComplete: SubmissionResult => Unit): Unit
}

