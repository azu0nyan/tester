package tester.srv.controller.impl

import clientRequests.admin.CustomProblemUpdateData
import clientRequests.admin.ProblemTemplateList.ProblemTemplateFilter
import doobie.implicits.*
import doobie.util.transactor
import io.github.gaelrenoux.tranzactio.DbException
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import otsbridge.ProblemScore.BinaryScore
import otsbridge.{AnswerField, ProblemInfo, ProblemTemplate}
import tester.srv.controller.{CourseTemplateRegistry, MessageBus, ProblemInfoRegistry, ProblemService, ProblemTemplateService}
import tester.srv.dao.DbProblemTemplateDao
import viewData.ProblemTemplateExampleViewData
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

  def applyFilter(filter: ProblemTemplateFilter, infos: Seq[ProblemInfo]):TranzactIO[Seq[ProblemInfo]] =
    filter match
      case ProblemTemplateFilter.AliasOrTitleMatches(regex) =>
        ZIO.succeed(infos.filter( i => i.alias.matches(regex) || i.title(0).matches(regex)))
      case ProblemTemplateFilter.TextContains(text) =>
        ZIO.succeed(infos.filter(_.problemHtml(0).contains(text)))
      case ProblemTemplateFilter.Editable(editable) =>
        ZIO.succeed(infos.filter(_.editable == editable))
      case ProblemTemplateFilter.FromCourseTemplate(template) =>
        courseTemplateRegistry.courseTemplate(template).map {
          case Some(ct) => infos.filter(t => ct.problemAliasesToGenerate.contains(t.alias))
          case None => Seq()
        }

  def list(filters: Seq[ProblemTemplateFilter]): TranzactIO[Seq[ProblemTemplateExampleViewData]] =
    for {
      infos <- registry.allInfos
      res <- ZIO.foldLeft(filters)(infos)((is, f) => applyFilter(f, is))
    } yield res.map(ProblemTemplateServiceImpl.toProblemTemplateExampleViewData)

}

object ProblemTemplateServiceImpl {

  def toProblemTemplateExampleViewData(info: ProblemInfo): ProblemTemplateExampleViewData =
    viewData.ProblemTemplateExampleViewData(
      info.title(0),
      info.initialScore,
      info.alias,
      info.allowedAttempts,
      info.problemHtml(0),
      info.answerField(0),
      info.editable
    )

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