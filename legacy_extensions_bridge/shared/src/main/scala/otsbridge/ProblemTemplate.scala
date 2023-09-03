package otsbridge


import otsbridge.AnswerField.AnswerField
import otsbridge.AnswerVerificationResult.Verified
import otsbridge.ProblemScore.ProblemScore

import scala.concurrent.Future

object ProblemTemplate {
  type ProblemTemplateAlias = String

  trait ProblemVerifiedByTeacher extends ProblemTemplate {
    override val requireTeacherVerificationIfScoreGEQThan: Option[Int] = Some(0)
    override final def verifyAnswer(seed: Int, answer: String): AnswerVerificationResult =
      Verified(initialScore, None)
  }

}

trait ProblemInfo {
  def title(seed: Int): String
  def alias: String
  def maxAttempts: Option[Int] = allowedAttempts
  def allowedAttempts: Option[Int] = None
  def initialScore: ProblemScore
  def requireConfirmation: Boolean = requireTeacherVerificationIfScoreGEQThan.nonEmpty
  def requireTeacherVerificationIfScoreGEQThan: Option[Int] = None
  def problemHtml(seed: Int): String
  def answerField(seed: Int): AnswerField

  def editable:Boolean = false
}


trait ProblemTemplate extends ProblemInfo {
  def verifyAnswer(seed: Int, answer: String): AnswerVerificationResult
}

