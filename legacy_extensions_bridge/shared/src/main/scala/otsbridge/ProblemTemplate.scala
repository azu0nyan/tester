package otsbridge


import otsbridge.ProblemScore.ProblemScore

import scala.concurrent.Future

object ProblemTemplate {
  type ProblemTemplateAlias = String

  trait ProblemVerifiedByTeacher extends ProblemTemplate {
    override final val requireTeacherVerification: Boolean = true
    override final def verifyAnswer(seed: Int, answer: String): AnswerVerificationResult = Verified(initialScore, Some("Ожидает проверки преподавателем"))
  }

}


trait ProblemTemplate {

  def title(seed: Int): String
  val initialScore: ProblemScore
  val uniqueAlias: String
  val allowedAttempts: Option[Int] = None
  val requireTeacherVerification: Boolean = false
  //def answerFromString[AT](field: AnswerFieldType[AT]): Option[AT] = ???
  //def generateProblem(seed: Int): ProblemInstance = ProblemInstance.create()
  def problemHtml(seed: Int): String
  def answerField(seed: Int): AnswerField
  def verifyAnswer(seed: Int, answer: String): AnswerVerificationResult

}

