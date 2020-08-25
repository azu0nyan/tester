package otsbridge

object CoursePiece {


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

  case class CourseMainPiece(title: String, annotation: String, themes: Seq[Theme]) extends Container {
    override def alias: String = "main"
    override def childs: Seq[CoursePiece] = themes
    override def displayMe: DisplayMe = OwnPage
  }


  case class Theme(
                    alias: String,
                    title: String,
                    textHtml: String,
                    subThemes: Seq[SubTheme]
                  ) extends Container {
    override def displayMe: DisplayMe = OwnPage
    override def childs: Seq[CoursePiece] = subThemes
  }

  case class SubTheme(
                       alias: String,
                       title: String,
                       textHtml: String,
                       childs: Seq[Problem]
                     ) extends Container {
    override def displayMe: DisplayMe = OwnPage
  }


  case class HtmlToDisplay(alias: String, displayMe: DisplayMe, htmlRaw:String) extends CoursePiece

  case class Problem(problemAlias: String, displayMe: DisplayMe) extends CoursePiece {
    override def alias: String = problemAlias
  }

}

