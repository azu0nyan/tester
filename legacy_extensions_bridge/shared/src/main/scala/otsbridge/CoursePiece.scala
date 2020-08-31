package otsbridge

import otsbridge.DisplayMe.{Inline, OwnPage}

object CoursePiece {
  val emptyCourse: CourseRoot = CourseRoot("", "", Seq())


  val pathDelimiter = "#"
  type PiecePath = Seq[String]
  def pathToString(path: PiecePath): String = path.mkString(pathDelimiter)
  def stringToPath(str: String): PiecePath = str.split(pathDelimiter).toSeq
  type PieceWithPath = (CoursePiece, PiecePath)

  sealed trait CoursePiece {
    def alias: String
    def displayMe: DisplayMe

    val displayInContentsHtml: Option[String] = None

    lazy val linearize: Seq[PieceWithPath] = Seq((this, Seq(alias)))

    lazy val pieceByPath:Map[String, CoursePiece] = linearize.map(x => (pathToString(x._2), x._1)).toMap

  }

  sealed trait Container extends CoursePiece {
    def childs: Seq[CoursePiece]
    override lazy val linearize: Seq[PieceWithPath] = (this, Seq(alias)) +:
      childs.flatMap(_.linearize).map { case (piece, path) => (piece, alias +: path)
      }
  }

  case class CourseRoot(title: String, annotation: String, childs: Seq[CoursePiece]) extends Container {
    override def alias: String = "main"
    override def displayMe: DisplayMe = OwnPage

    override val displayInContentsHtml: Option[String] = Some(s"$title")
  }


  case class Theme(
                    alias: String,
                    title: String,
                    textHtml: String = "",
                    childs: Seq[CoursePiece] = Seq(),
                    displayMe: DisplayMe = OwnPage
                  ) extends Container {
    //    override def displayMe: DisplayMe = OwnPage
    override val displayInContentsHtml: Option[String] = Some(s"$title")
  }

  case class SubTheme(
                       alias: String,
                       title: String,
                       textHtml: String = "",
                       childs: Seq[CoursePiece] = Seq(),
                       displayMe: DisplayMe = OwnPage
                     ) extends Container {
    override val displayInContentsHtml: Option[String] = Some(s"$title")
    //    override def displayMe: DisplayMe = OwnPage
  }


  case class HtmlToDisplay(alias: String, displayMe: DisplayMe, htmlRaw: String) extends CoursePiece

  case class TextWithHeading(alias: String, heading: String, bodyHtml: String, displayMe: DisplayMe = Inline) extends CoursePiece

  case class Paragraph(alias: String, bodyHtml: String, displayMe: DisplayMe = Inline) extends CoursePiece

  case class Problem(problemAlias: String, displayMe: DisplayMe) extends CoursePiece {
//    override val displayInContentsHtml: Option[String] = Some(s"<h2>$contentsTitle</h2>")
    override def alias: String = problemAlias
  }

}

