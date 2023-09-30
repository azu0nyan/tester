package tester.srv.controller.impl

import clientRequests.admin.CustomProblemUpdateData
import doobie.implicits.*
import doobie.util.transactor
import io.github.gaelrenoux.tranzactio.DbException
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import otsbridge.ProblemScore.BinaryScore
import otsbridge.{AnswerField, ProblemTemplate}
import tester.srv.controller.{CourseTemplateRegistry, MessageBus, ProblemInfoRegistry, ProblemService, ProblemTemplateService}
import tester.srv.dao.DbProblemTemplateDao
import zio.*

case class ProblemTemplateServiceImpl(
                                       bus: MessageBus,
                                       registry: ProblemInfoRegistry,
                                       courseTemplateRegistry: CourseTemplateRegistry
                                     ) extends ProblemTemplateService {
  def newSimpleTemplate(alias: String, title: String, html: String): TranzactIO[Boolean] =
    DbProblemTemplateDao.insert(DbProblemTemplateDao.DbProblemTemplate(
      alias, title, html,
      AnswerField.TextField(",", 10).toJson,
      BinaryScore(false).toJson, true, None, None))

  def removeTemplateCascade(alias: String): TranzactIO[Boolean] = ???

  def reloadFromDb(alias: String): TranzactIO[Unit] =
    DbProblemTemplateDao.byAlias(alias)
      .flatMap(t => registry.registerProblemInfo(DbProblemTemplateDao.toProblemInfo(t)))

  def updateTemplate(alias: String, updateData: CustomProblemUpdateData): TranzactIO[Boolean] =
    for {
      res <- DbProblemTemplateDao.updateByAlias(alias,
        fr"title=${updateData.title}, html=${updateData.html}, answerField=${updateData.answerField.toJson}, initialScore=${updateData.initialScore.toJson}")
      _ <- ZIO.when(res)(reloadFromDb(alias))
    } yield res
}

object ProblemTemplateServiceImpl {
  val live: URIO[MessageBus & ProblemInfoRegistry & CourseTemplateRegistry, ProblemTemplateService] =
    for {
      bus <- ZIO.service[MessageBus]
      reg <- ZIO.service[ProblemInfoRegistry]
      ct <- ZIO.service[CourseTemplateRegistry]
      srv = ProblemTemplateServiceImpl(bus, reg, ct)
    } yield srv

  def layer: URLayer[MessageBus & ProblemInfoRegistry & CourseTemplateRegistry, ProblemTemplateService] =
    ZLayer.fromZIO(live)
}