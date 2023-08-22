package tester.srv.dao

import io.github.gaelrenoux.tranzactio.doobie.{TranzactIO, tzio}
import CourseTemplateProblemDao.CourseTemplateProblem
import zio.schema.{DeriveSchema, Schema}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*


object CourseTemplateProblemDao extends AbstractDao [CourseTemplateProblem]{
  case class CourseTemplateProblem(courseAlias: String, problemAlias: String)

  override val schema: Schema[CourseTemplateProblem] = DeriveSchema.gen[CourseTemplateProblem]
  override val tableName: String = "CourseTemplateProblem"

  def templateProblemAliases(alias: String): TranzactIO[Seq[CourseTemplateProblem]] =
    selectWhereAndList(fr"courseAlias = $alias")

  def removeProblemFromTemplate(courseAlias: String, problemAlias: String): TranzactIO[Boolean] =
    deleteWhere(fr"courseAlias = $courseAlias AND problemAlias = $problemAlias")
      .map(_ == 1)

}
