package tester.srv.controller.impl


import zio.*
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import io.github.gaelrenoux.tranzactio.{DbException, doobie}
import doobie.{Connection, Database, TranzactIO, tzio}
import otsbridge.ProblemScore.{BinaryScore, ProblemScore}
import tester.srv.controller.{MessageBus, ProblemInfoRegistry, ProblemService}
import tester.srv.dao.ProblemDao
import tester.srv.dao.ProblemDao.Problem


case class ProblemServiceImpl private(
                                             bus: MessageBus,
                                             infoRegistryZIO: ProblemInfoRegistry,
                                           ) extends ProblemService {

  def startProblem(courseId: Int, templateAlias: String): TranzactIO[Int] = {
    for {
      info <- infoRegistryZIO.problemInfo(templateAlias)
      res <- info match
        case Some(info) =>
          val toInsert = Problem(0, courseId, templateAlias, scala.util.Random.nextInt(),
            info.initialScore.toJson, info.initialScore.percentage, None, None, info.requireConfirmation)
          ProblemDao.insertReturnId(toInsert)
        case None =>
          ZIO.die(new Exception(s"Cant find problem with alias $templateAlias"))
    } yield res
  }

  def removeProblem(courseId: Int, templateAlias: String): TranzactIO[Boolean] =
    for {
      problem <- ProblemDao.byCourseAndTemplate(courseId, templateAlias)
      res <- ZIO.when(problem.nonEmpty)(ProblemDao.deleteById(problem.get.id))
    } yield res.getOrElse(false)


  def reportAnswerConfirmed(problemId: Int, answerId: Int, score: ProblemScore): TranzactIO[Unit] =
    for{
      p <- ProblemDao.byId(problemId)
      _ <- ZIO.when(p.scoreNormalized < score.percentage)(ProblemDao.updateScore(problemId, score))
    } yield ()

}

object ProblemServiceImpl {
  val live: URIO[MessageBus & ProblemInfoRegistry, ProblemService] =
    for {
      bus <- ZIO.service[MessageBus]
      reg <- ZIO.service[ProblemInfoRegistry]
      srv = ProblemServiceImpl(bus, reg)
      //        _ <- (for{
      //          sub <- bus.answerConfirmations.subscribe
      //          taken <- sub.take
      //          _ <- db.transactionOrWiden(srv.reportAnswerConfirmed(taken.problemId, taken.answerId, taken.score)).catchAll(_ => ZIO.succeed(()))
      //        } yield ())
    } yield srv

  def layer: URLayer[MessageBus & ProblemInfoRegistry, ProblemService] =
    ZLayer.fromZIO(live)
}
