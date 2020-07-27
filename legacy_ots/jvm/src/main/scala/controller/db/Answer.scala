package controller.db

import java.time.{Clock, Instant}


import controller.db.Answer.AnswerStatus
import .ProblemScore
import org.bson.types.ObjectId

object Answer {
  sealed trait AnswerStatus
  case class Verified(score: ProblemScore,
                      review: Option[String] = None,
                      systemMessage: Option[String] = None,
                      verifiedAt: Instant) extends AnswerStatus
  case class Rejected(systemMessage: Option[String] = None, rejectedAt: Instant) extends AnswerStatus
  case class BeingVerified() extends AnswerStatus
  def apply(problemId: ObjectId, answer: String, status: AnswerStatus, answeredAt: Instant): Answer = new Answer(new ObjectId(), problemId, answer, status, answeredAt)
}


case class Answer(_id:ObjectId, problemId:ObjectId, answer: String, status: AnswerStatus, answeredAt: Instant) extends MongoObject {
  def changeStatus(newStatus: AnswerStatus): Answer = {
    answers.updateField(this, "status", newStatus)
    this.copy(status = newStatus)
  }
}
