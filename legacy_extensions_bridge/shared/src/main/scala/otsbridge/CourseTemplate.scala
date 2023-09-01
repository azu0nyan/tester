package otsbridge

import otsbridge.CoursePiece._


trait CourseTemplate {

  def editable:Boolean = false

  def description: String = ""

  def problemAliasesToGenerate:Seq[String] = problemAliasesToGenerateRec(courseData)

  def courseData:CourseRoot

  val courseTitle: String

  val uniqueAlias: String

  private def problemAliasesToGenerateRec(cp:CoursePiece):Seq[String] = cp match {
    case container: CoursePiece.Container => container.childs.flatMap(problemAliasesToGenerateRec)
    case CoursePiece.Problem(problemAlias, displayMe, contents) => Seq(problemAlias)
    case _ => Seq()
  }


}
object CourseTemplate {
  case class CourseTemplateData(uniqueAlias: String,
                                courseTitle: String = "",
                                override val description: String = "",
                                override val courseData: CourseRoot = CourseRoot("NO TITLE", "", Seq()), problemAliases: Seq[String] = Seq()) extends CourseTemplate
}


