package otsbridge

object CoursePiece {
  val emptyCourse: CourseRoot = CourseRoot("", "", Seq())


  sealed trait DisplayMe
  case object OwnPage extends DisplayMe
  case object Inline extends DisplayMe

  sealed trait CoursePiece {
    def alias: String
    def displayMe: DisplayMe
  }

  sealed trait Container extends CoursePiece {
    def childs: Seq[CoursePiece]
  }

  case class CourseRoot(title: String, annotation: String, childs: Seq[CoursePiece] ) extends Container {
    override def alias: String = "main"
    override def displayMe: DisplayMe = OwnPage
  }


  case class Theme(
                    alias: String,
                    title: String,
                    textHtml: String = "",
                    childs: Seq[CoursePiece]= Seq(),
                    displayMe:DisplayMe = OwnPage
                  ) extends Container {
//    override def displayMe: DisplayMe = OwnPage
  }

  case class SubTheme(
                       alias: String,
                       title: String,
                       textHtml: String = "",
                       childs: Seq[CoursePiece] = Seq(),
                       displayMe:DisplayMe = OwnPage
                     ) extends Container {
//    override def displayMe: DisplayMe = OwnPage
  }


  case class HtmlToDisplay(alias: String, displayMe: DisplayMe, htmlRaw:String) extends CoursePiece

  case class TextWithHeading(alias:String,heading:String, bodyHtml:String,  displayMe: DisplayMe = Inline) extends CoursePiece
  case class Paragraph(alias:String, bodyHtml:String,  displayMe: DisplayMe = Inline) extends CoursePiece

  case class Problem(problemAlias: String, displayMe: DisplayMe) extends CoursePiece {
    override def alias: String = problemAlias
  }

}

