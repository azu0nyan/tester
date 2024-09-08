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
import otsbridge.{ProblemInfo, ProblemScore}
import otsbridge.ProblemScore.{BinaryScore, ProblemScore}
import tester.srv.controller.AnswerService.{AnswerFilterParams, AnswerMetaStatus}
import tester.srv.controller.{MessageBus, ProblemInfoRegistry, ProblemService}
import tester.srv.dao.AnswerDao
import tester.srv.dao.ProblemDao
import tester.srv.dao.ProblemDao.Problem
import viewData.ProblemViewData

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
            info.initialScore.toJson, info.initialScore.percentage, None, None, info.requireConfirmation, java.time.Clock.systemUTC().instant())
          ProblemDao
            .insertReturnId(toInsert)
            .tapError(error => ZIO.logErrorCause(error.getMessage, Cause.die(error)))
        case None =>
          ZIO.die(new Exception(s"Cant find problem with alias $templateAlias"))
      _ <- ZIO.log(s"Inserted problem id: $res course id: $courseId template alias: $templateAlias")
    } yield res
  }

  def removeProblem(courseId: Int, templateAlias: String): TranzactIO[Boolean] =
    for {
      problem <- ProblemDao.byCourseAndTemplate(courseId, templateAlias)
      res <- ZIO.when(problem.nonEmpty)(ProblemDao.deleteById(problem.get.id))
    } yield res.getOrElse(false)


  def reportAnswerConfirmed(problemId: Int, answerId: Int, score: ProblemScore): TranzactIO[Unit] =
    for {
      p <- ProblemDao.byId(problemId)
      _ <- ZIO.when(p.scoreNormalized < score.percentage)(ProblemDao.updateScore(problemId, score))
    } yield ()

  def setScore(problemId: Int, score: ProblemScore): TranzactIO[Boolean] =
    ProblemDao.updateScore(problemId, score)

  def getRefViewData(problemId: Int): TranzactIO[Option[viewData.ProblemRefViewData]] =
    for {
      problem <- ProblemDao.byId(problemId)
      templateOpt <- infoRegistryZIO.problemInfo(problem.templateAlias)
    } yield templateOpt.map(template =>
      viewData.ProblemRefViewData(problemId.toString, problem.templateAlias, template.title(problem.seed),
        ProblemScore.fromJson(problem.score)))

  def getViewData(problemId: Int): TranzactIO[Option[ProblemViewData]] =
    (for {
      problemOpt <- ProblemDao
        .byIdOption(problemId)
        .tapSome{case None => ZIO.logError(s"Problem with id $problemId requested but not found in DB")}
      templateAlias = problemOpt.get.templateAlias
      templateOpt <- infoRegistryZIO
        .problemInfo(templateAlias)
        .tapSome{case None => ZIO.logError(s"Problem with alias $templateAlias alias requested but not found in registry")}
    } yield (problemOpt, templateOpt)).flatMap {
      case (Some(problem), Some(template)) =>
        (for {
          answers <- AnswerDao.queryAnswers(AnswerFilterParams(problemId = Some(problemId)))()
          title <- ZIO.attempt(template.title(problem.seed))
          html <- ZIO.attempt(template.problemHtml(problem.seed))
          answerField <- ZIO.attempt(template.answerField(problem.seed))
        } yield Some(ProblemViewData(problemId.toString, problem.templateAlias,
          title, html, answerField,
          ProblemScore.fromJson(problem.score), answers.lastOption.map(_._1.answer).getOrElse(""),
          answers.map { case (a, b, c) => AnswerMetaStatus(a, b, c.toStatus).toViewData }
        ))).catchAll {
          case dbException: DbException => ZIO.fail(dbException)
          case other: Throwable => ZIO.succeed(None)
        }
      case _ =>
        ZIO.succeed(None)
    }

  def registerInfo(info: ProblemInfo): UIO[Unit] =
    infoRegistryZIO.registerProblemInfo(info)
      
}

object ProblemServiceImpl {
  val live: URIO[MessageBus & ProblemInfoRegistry, ProblemService] =
    for {
      bus <- ZIO.service[MessageBus]
      reg <- ZIO.service[ProblemInfoRegistry]
      srv = ProblemServiceImpl(bus, reg)
    } yield srv

  def layer: URLayer[MessageBus & ProblemInfoRegistry, ProblemService] =
    ZLayer.fromZIO(live)
}
