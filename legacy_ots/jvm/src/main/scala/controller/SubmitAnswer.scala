package controller

import java.time.Clock

import DbViewsShared.CourseShared._
import controller.db.{Answer, Problem, User}
import otsbridge.AnswerVerificationResult
//import otsbridge.{AnswerVerificationResult, CantVerify, VerificationDelayed, Verified}
import org.mongodb.scala.bson.ObjectId
import cats.implicits._
import clientRequests._
//import com.typesafe.scalalogging.Logger
//import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object SubmitAnswer {
  import com.typesafe.scalalogging.Logger
  import org.slf4j.LoggerFactory
  val log: Logger = Logger(LoggerFactory.getLogger("controller.SubmitAnswer"))

  def submitAnswer(req: SubmitAnswerRequest): SubmitAnswerResponse = {
    LoginUserOps.decodeAndValidateToken(req.token) match {
      case Some(user) =>
        UsersRegistry.doSynchronized[SubmitAnswerResponse](user._id) {
          db.problems.byId(new ObjectId(req.problemIdHex)) match {
            case Some(problem) =>
              val course = db.courses.byId(problem.courseId)
              if (course.isEmpty) {
                log.error(s"CRITICAL! possible DB corruption, there shouldn't been existing problem linked for existing course. \n " +
                  s"User ${user.idAndLoginStr} tried to submit answer for non-existent course")
                UserCourseWithProblemNotFound()
              } else if (course.get.userId != user._id) {
                log.error(s"User ${user.idAndLoginStr} tried to submit answer for problem in course he doesn't own")
                ProblemIsNotFromUserCourse()
              } else {
                val submittedAnswerCount = problem.answers.count(a =>
                  a.status.isInstanceOf[BeingVerified] || a.status.isInstanceOf[Verified])
                if(problem.attemptsMax.nonEmpty && submittedAnswerCount >= problem.attemptsMax.get){
                  MaximumAttemptsLimitExceeded(problem.attemptsMax.get)
                } else {
                  val answer = db.answers.insert(Answer(problem._id, req.answerRaw, BeingVerified(), Clock.systemUTC().instant())).pure[Option]
                  log.info(s"User ${user.idAndLoginStr} submitted answer ${answer.map(_._id.toHexString).getOrElse("NONE")} for problem ${problem.idAlias} from course ${course.get.idAlias}")
                  val template = TemplatesRegistry.getProblemTemplate(problem.templateAlias).get
                  Future {
                    template.verifyAnswer(problem.seed, req.answerRaw)
                  }.map(processSubmissionResult(_, answer.get, user))
                  AnswerSubmitted()
                }
              }
            case None =>
              log.error(s"user ${user.idAndLoginStr} tried to submit answer for non-existent problem ${req.problemIdHex}")
              ProblemNotFound()
          }
        }
      case None => RequestSubmitAnswerFailure(BadToken())
    }
  }

  /** main function for processing answers,
   * synchronization to prevent insertion of multiple  answers
   * attemptsLeft should be greater than 'BeingVerified' answers  */
 /* def submitAnswer(problemIdHex: String, answerRaw: String, user: User): Unit = {
    log.info(s"Answer submitted id : $problemIdHex answer : $answerRaw user ${user._id.toHexString} login : ${user.login}")
    UsersRegistry.doSynchronized(user._id) {
      //val answer: Option[Answer] = db.answers.insert(Answer())byId(new ObjectId(answerIdHex))
      for (
        p <- db.problems.byId(new ObjectId(problemIdHex))
        if p.attemptsMax.nonEmpty &&
          db.answers.byField("problemId", p._id)
            .count(_.status.isInstanceOf[BeingVerified]) < p.attemptsMax.get;
        pl <- db.courses.byId(p.courseId) if pl.userId.equals(user._id);
        pt <- TemplatesRegistry.getProblemTemplate(p.templateAlias);
        answer <- db.answers.insert(Answer(p._id, answerRaw, BeingVerified(), Clock.systemUTC().instant())).pure[Option]
      ) {
        log.info(s"Answer : ${answer._id}  for problem $problemIdHex added to DB and submitted")
        pt.verifyAnswer(p.seed, answerRaw, processSubmissionResult(_, answer, user))
      }
    }
  }
*/

  def processSubmissionResult(sr: AnswerVerificationResult, answer: Answer, user: User): Unit = UsersRegistry.doSynchronized(user._id) {
    sr match {
      case otsbridge.Verified(score, review, systemMessage) =>
        log.info(s"Answer : ${answer._id} verified")
        answer.changeStatus(Verified(score, review, systemMessage, Clock.systemUTC().instant()))
        db.problems.byId(answer.problemId).foreach { p =>
          val bestScore = otsbridge.CompareProblemScore.bestOf(p.score, score)
          if (p.score != bestScore) p.updateScore(score)
        }
      case otsbridge.CantVerify(systemMessage) =>
        log.info(s"Answer : ${answer._id} cant verify cause : ${systemMessage.getOrElse("No message, unknown")}")
        answer.changeStatus(Rejected(systemMessage, Clock.systemUTC().instant()))
      case otsbridge.VerificationDelayed(systemMessage) =>
        log.info(s"Answer : ${answer._id} verification delayed")

    }
  }
}