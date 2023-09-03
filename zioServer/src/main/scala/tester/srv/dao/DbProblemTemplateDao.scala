package tester.srv.dao


import AbstractDao.*
import DbProblemTemplateDao.DbProblemTemplate
import doobie.implicits.*
import zio.schema.{DeriveSchema, Schema}
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import otsbridge.{AnswerField, ProblemInfo, ProblemScore}

object DbProblemTemplateDao extends AbstractDao[DbProblemTemplate]
  with ByAlias[DbProblemTemplate] {

  case class DbProblemTemplate(alias: String, title: String, html: String,
                               answerField: String, initialScore: String,
                               requireConfirmation: Boolean, maxAttempts: Option[Int], verificatorAlias: Option[String])
  
  def toProblemInfo(t: DbProblemTemplate): ProblemInfo = new ProblemInfo {
    override def title(seed: Int) = t.title
    override def alias = t.alias
    override def initialScore = ProblemScore.fromJson(t.initialScore)
    override def problemHtml(seed: Int) = t.html
    override def answerField(seed: Int) = AnswerField.fromJson(t.answerField)
  }

  override val schema: Schema[DbProblemTemplate] = DeriveSchema.gen[DbProblemTemplate]
  override val tableName: String = "ProblemTemplate"
  override def jsonFields: Seq[String] = Seq("answerField", "initialScore")

  def setTitle(alias: String, title: String): TranzactIO[Boolean] =
    updateByAlias(alias, fr"title=$title")

  def setHtml(alias: String, html: String): TranzactIO[Boolean] =
    updateByAlias(alias, fr"html=$html")



}

