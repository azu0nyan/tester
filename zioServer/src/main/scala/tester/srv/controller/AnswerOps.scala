package tester.srv.controller

import zio.*
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import io.github.gaelrenoux.tranzactio.{DbException, doobie}
import doobie.{Connection, Database, TranzactIO, tzio}

import java.time.Instant


object AnswerOps {
  case class Answer(id: Long, problemId: Long, answer: String, status: String, answeredAt: Instant)

  val answerFields = fr"id, problemId, answer, status, answeredAt"
  val answerSelect = fr"SELECT $answerFields FROM Answer"

  def removeAnswer(id: Long) = tzio {
    sql"""DELETE FROM Answer WHERE id = $id""".update.run
  }

  def problemAnswers(problemId: Long): TranzactIO[List[Answer]] = tzio {
    (answerSelect ++ fr"WHERE problemId = $problemId").query[Answer].to[List]
  }
  
  def deleteProblemAnswers(problemId: Long) = tzio{
    sql"""DELETE FROM Answer WHERE problemId = $problemId""".update.run
  }

}
