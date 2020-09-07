package otsbridge

import otsbridge.CoursePiece.{CoursePiece, CourseRoot}
import otsbridge.ProblemScore.ProblemScore




trait CourseTemplate {

  def description: Option[String] = None

  def problemAliasesToGenerate:Seq[String] = problemAliasesToGenerateRec(courseData)

  def courseData:CourseRoot

  val courseTitle: String

  val uniqueAlias: String

  private def problemAliasesToGenerateRec(cp:CoursePiece):Seq[String] = cp match {
    case container: CoursePiece.Container => container.childs.flatMap(problemAliasesToGenerateRec)
    case CoursePiece.Problem(problemAlias, displayMe) => Seq(problemAlias)
  }

}



