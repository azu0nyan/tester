package otsbridge


import scala.concurrent.Future
object ProblemTemplate{
  type ProblemTemplateAlias = String
}


trait ProblemTemplate {
  def title(seed:Int): String
  val initialScore: ProblemScore
  val uniqueAlias: String
  val allowedAttempts: Option[Int] = None
  //def answerFromString[AT](field: AnswerFieldType[AT]): Option[AT] = ???
  //def generateProblem(seed: Int): ProblemInstance = ProblemInstance.create()
  def generateProblemHtml(seed: Int): String
  def answerField(seed: Int): AnswerField
  def verifyAnswer(seed: Int, answer: String): AnswerVerificationResult
}

