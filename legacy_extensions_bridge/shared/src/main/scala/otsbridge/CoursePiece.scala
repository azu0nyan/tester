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

    lazy val pieceByPath: Map[String, CoursePiece] = linearize.map(x => (pathToString(x._2), x._1)).toMap

    def fullHtml(aliasToPt:Map[String, ProblemTemplate]):String = this match {
      case HtmlToDisplay(alias, displayMe, htmlRaw) => htmlRaw
      case TextWithHeading(alias, heading, bodyHtml, displayMe, contents) =>
        raw"""<div>
             |<h1>$heading</h1>
             |$bodyHtml
             |</div>
             |""".stripMargin

      case Paragraph(alias, bodyHtml, displayMe) => bodyHtml
      case Problem(problemAlias, displayMe, contents) =>
        raw"""<div>
             |<h1>${ aliasToPt.get(alias).map(_.title(0)).getOrElse("")}</h1>
             |${ aliasToPt.get(alias).map(_.problemHtml(0)).getOrElse("")}
             |</div>
             |""".stripMargin

      case Theme(_, title, text, childs, _) =>
        raw"""<div>
             |<h1>$title</h1>
             |$text
             |${childs.map(_.fullHtml(aliasToPt)).reduce(_ + _)}
             |</div>
             |""".stripMargin
      case SubTheme(_, title, text, childs, _) =>
        raw"""<div>
             |<h1>$title</h1>
             |$text
             |${childs.map(_.fullHtml(aliasToPt)).reduceOption(_ + _).getOrElse("")}
             |</div>
             |""".stripMargin

      case container: Container => container.childs.map(_.fullHtml(aliasToPt)).reduceOption(_ + _).getOrElse("")
    }

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

  case class TextWithHeading(alias: String, heading: String, bodyHtml: String, displayMe: DisplayMe = Inline, override val displayInContentsHtml: Option[String] = None) extends CoursePiece

  @Deprecated
  case class ContainerWithHeading(alias: String, heading: String, bodyHtml: String, displayMe: DisplayMe = Inline,childs: Seq[CoursePiece] = Seq()) extends Container



  case class Paragraph(alias: String, bodyHtml: String, displayMe: DisplayMe = Inline) extends CoursePiece

  object Problem {
    def apply(pt: ProblemTemplate, displayMe: DisplayMe, inContents:Option[String] = None): Problem =
      Problem(pt.uniqueAlias, displayMe, inContents)
  }

  case class Problem(problemAlias: String, displayMe: DisplayMe, override val displayInContentsHtml: Option[String]) extends CoursePiece {
    override def alias: String = problemAlias
  }

}

