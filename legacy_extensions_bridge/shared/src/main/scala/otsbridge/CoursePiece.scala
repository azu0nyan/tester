package otsbridge

import otsbridge.DisplayMe.{Inline, OwnPage}

object CoursePiece {

  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  //  implicit val pieceDecoder: Decoder[CoursePiece] = deriveDecoder[CoursePiece]
  //  implicit val pieceEncoder: Encoder[CoursePiece] = deriveEncoder[CoursePiece]

  //  implicit val reqDec: Decoder[CourseRoot] = deriveDecoder[CourseRoot]
  //  implicit val resEnc: Encoder[CourseRoot] = deriveEncoder[CourseRoot]

  implicit val reqDec1: Decoder[HtmlToDisplay] = deriveDecoder[HtmlToDisplay]
  implicit val resEnc1: Encoder[HtmlToDisplay] = deriveEncoder[HtmlToDisplay]
  implicit val reqDec2: Decoder[TextWithHeading] = deriveDecoder[TextWithHeading]
  implicit val resEnc2: Encoder[TextWithHeading] = deriveEncoder[TextWithHeading]
  implicit val reqDec3: Decoder[Paragraph] = deriveDecoder[Paragraph]
  implicit val resEnc3: Encoder[Paragraph] = deriveEncoder[Paragraph]
  implicit val reqDec4: Decoder[Problem] = deriveDecoder[Problem]
  implicit val resEnc4: Encoder[Problem] = deriveEncoder[Problem]
  implicit val reqDec5: Decoder[DisplayMe] = deriveDecoder[DisplayMe]
  implicit val resEnc6: Encoder[DisplayMe] = deriveEncoder[DisplayMe]

  import cats.syntax.functor._
  import io.circe.{Decoder, Encoder}, io.circe.generic.auto._
  import io.circe.syntax._


  implicit val encodeTheme: Encoder[Theme] = Encoder.instance {
    case r@Theme(alias, title, textHtml, childs, displayMe) =>
      Json.obj(
        ("alias", Json.fromString(alias)),
        ("title", Json.fromString(title)),
        ("textHtml", Json.fromString(textHtml)),
        ("childs", childs.asJson),
        ("displayMe", displayMe.asJson),
      )
  }

  implicit val decodeTheme: Decoder[Theme] = new Decoder[Theme]:
    override def apply(c: HCursor) =
      for {
        a <- c.downField("alias").as[String]
        t <- c.downField("title").as[String]
        th <- c.downField("textHtml").as[String]
        cp <- c.downField("childs").as[Seq[CoursePiece]]
        d <- c.downField("displayMe").as[DisplayMe]
      } yield Theme(a, t, th, cp, d)


  implicit val encodeSubTheme: Encoder[SubTheme] = Encoder.instance {
    case r@SubTheme(alias, title, textHtml, childs, displayMe) =>
      Json.obj(
        ("alias", Json.fromString(alias)),
        ("title", Json.fromString(title)),
        ("textHtml", Json.fromString(textHtml)),
        ("childs", childs.asJson),
        ("displayMe", displayMe.asJson),
      )
  }

//  implicit val decodeHtmlToDisplay: Decoder[HtmlToDisplay] = new Decoder[HtmlToDisplay]:
//    override def apply(c: HCursor) =
//      for {
//        a <- c.downField("alias").as[String]
//        d <- c.downField("displayMe").as[DisplayMe]
//        h <- c.downField("htmlRaw").as[String]
//      } yield HtmlToDisplay(a, d, h)
//
//
//  implicit val encodeHtmlToDisplay: Encoder[HtmlToDisplay] = Encoder.instance {
//    case r@HtmlToDisplay(alias, displayMe, htmlRaw) =>
//      Json.obj(
//        ("alias", Json.fromString(alias)),
//        ("displayMe", displayMe.asJson),
//        ("htmlRaw", Json.fromString(htmlRaw)),
//      )
//  }

  implicit val decodeSubTheme: Decoder[SubTheme] = new Decoder[SubTheme]:
    override def apply(c: HCursor) =
      for {
        a <- c.downField("alias").as[String]
        t <- c.downField("title").as[String]
        th <- c.downField("textHtml").as[String]
        cp <- c.downField("childs").as[Seq[CoursePiece]]
        d <- c.downField("displayMe").as[DisplayMe]
      } yield SubTheme(a, t, th, cp, d)

  implicit val pieceDecoder: Decoder[CoursePiece] =
    new Decoder[CoursePiece]:
      override def apply(c: HCursor)  =
        c.keys.map(_.toSeq) match
          case Some(x) if x.isEmpty => Left(DecodingFailure("No key", List()))
          case Some(x) if x.size > 1 => Left(DecodingFailure("To many keys $x", List()))
          case None => Left(DecodingFailure("", List()))
          case Some(Seq("Theme")) => decodeTheme.tryDecode(c.downField("Theme"))
          case Some(Seq("SubTheme")) => decodeSubTheme.tryDecode(c.downField("SubTheme"))
          case Some(Seq("HtmlToDisplay")) => Decoder[HtmlToDisplay].tryDecode(c.downField("HtmlToDisplay"))
          case Some(Seq("TextWithHeading")) => Decoder[TextWithHeading].tryDecode(c.downField("TextWithHeading"))
          case Some(Seq("Paragraph")) => Decoder[Paragraph].tryDecode(c.downField("Paragraph"))
          case Some(Seq("Problem")) => Decoder[Problem].tryDecode(c.downField("Problem"))
          case Some(_) => Left(DecodingFailure("Unknown field", List()))

  /*{
    List[Decoder[CoursePiece]](
      Decoder[Theme].widen,
      Decoder[SubTheme].widen,
      Decoder[HtmlToDisplay].widen,
      Decoder[TextWithHeading].widen,
      Decoder[Paragraph].widen,
      Decoder[Problem].widen,
    ).reduceLeft (_ or _)
  }*/

  implicit val pieceEncoder: Encoder[CoursePiece] = Encoder.instance {
    case t@Theme(alias, title, textHtml, childs, displayMe) => Json.obj(("Theme", t.asJson))
    case s@SubTheme(alias, title, textHtml, childs, displayMe) => Json.obj(("SubTheme", s.asJson))
    case h@HtmlToDisplay(alias, displayMe, htmlRaw) => Json.obj(("HtmlToDisplay", h.asJson))
    case t@TextWithHeading(alias, heading, bodyHtml, displayMe, displayInContentsHtml) => Json.obj(("TextWithHeading", t.asJson))
    case p@Paragraph(alias, bodyHtml, displayMe) => Json.obj(("Paragraph", p.asJson))
    case p@Problem(problemAlias, displayMe, displayInContentsHtml) => Json.obj(("Problem", p.asJson))
    case c@CourseRoot(_, _, _) => Json.obj(("CourseRoot", c.asJson))//Не должно вызыватсья
  }


  implicit val encodeRoot: Encoder[CourseRoot] = Encoder.instance {
    case r@CourseRoot(title, annotation, childs) =>
      Json.obj(
        ("title", Json.fromString(title)),
        ("annotation", Json.fromString(annotation)),
        ("childs", childs.asJson),
      )
  }

  implicit val decodeRoot: Decoder[CourseRoot] = new Decoder[CourseRoot]:
    override def apply(c: HCursor) =
      for {
        title <- c.downField("title").as[String]
        a <- c.downField("annotation").as[String]
        c <- c.downField("childs").as[Seq[CoursePiece]]
      } yield CourseRoot(title, a, c)


  def fromJson(json: String): CourseRoot = {
    import io.circe.parser.decode
    decode[CourseRoot](json)(decodeRoot) match
      case Left(value) => throw new Exception(s"Cant decode course root $value $json")
      case Right(value) => value
  }


  val emptyCourse: CourseRoot = CourseRoot("", "", Seq())

  val pathDelimiter = "#"
  type PiecePath = Seq[String]
  def pathToString(path: PiecePath): String = path.mkString(pathDelimiter)
  def stringToPath(str: String): PiecePath = str.split(pathDelimiter).toSeq
  type PieceWithPath = (CoursePiece, PiecePath)

  sealed trait CoursePiece {

    def delete(aliasToDelete: String): CoursePiece = this match {
      case container: Container => container.setChilds(container.childs.filter(_.alias != aliasToDelete).map(_.delete(aliasToDelete)))
      case x => x
    }

    def findByAlias(toFind: String): Option[CoursePiece] = if (alias == toFind) Some(this) else this match {
      case container: Container => container.childs.flatMap(_.findByAlias(toFind)).headOption
      case _ => None
    }

    def alias: String
    def displayMe: DisplayMe

    def allAliaces: Set[String] = Set(alias)

    val displayInContentsHtml: Option[String] = None

    lazy val piecesOrdered: Seq[CoursePiece] = linearize.map(_._1)
    def next(cp: CoursePiece): Option[CoursePiece] = {
      val i = piecesOrdered.indexOf(cp)
      Option.when(i + 1 < piecesOrdered.size)(piecesOrdered(i + 1))
    }
    def prev(cp: CoursePiece): Option[CoursePiece] = {
      val i = piecesOrdered.indexOf(cp)
      Option.when(i >= 1)(piecesOrdered(i - 1))
    }


    lazy val linearize: Seq[PieceWithPath] = Seq((this, Seq(alias)))

    lazy val pieceByPath: Map[String, CoursePiece] = linearize.map(x => (pathToString(x._2), x._1)).toMap

    def fullHtml(aliasToPt: Map[String, ProblemTemplate]): String = this match {
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
             |<h1>${aliasToPt.get(alias).map(_.title(0)).getOrElse("")}</h1>
             |${aliasToPt.get(alias).map(_.problemHtml(0)).getOrElse("")}
             |</div>
             |""".stripMargin

      case Theme(_, title, text, childs, _) =>
        raw"""<div>
             |<h1>$title</h1>
             |$text
             |${childs.map(_.fullHtml(aliasToPt)).reduceOption(_ + _).getOrElse("")}
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

    def allProblems: Seq[Problem] = this match {
      case c: Container => c.childs.flatMap(_.allProblems)
      case problem: Problem => Seq(problem)
      case _ => Seq()
    }

  }


  sealed trait Container extends CoursePiece {
    def childs: Seq[CoursePiece]

    override def allAliaces: Set[String] = Set(alias) | childs.flatMap(_.allAliaces).toSet

    override lazy val linearize: Seq[PieceWithPath] = (this, Seq(alias)) +:
      childs.flatMap(_.linearize).map { case (piece, path) => (piece, alias +: path)
      }


    def replaceByAlias(aliasToReplace: String, replacement: CoursePiece): CoursePiece =
      if (alias == aliasToReplace) replacement
      else setChilds(childs.map({
        case container: Container => container.replaceByAlias(aliasToReplace, replacement)
        case c if c.alias == aliasToReplace => replacement
        case c => c
      }))

    def removeChildByAlias(alias: String): Container = {
      def removeByAliasFilter(e: CoursePiece): Option[CoursePiece] = {
        if (e.alias == alias) None
        else e match {
          case container: Container =>
            Some(container.removeChildByAlias(alias))
          case cp => Some(cp)
        }
      }
      setChilds(childs.flatMap(removeByAliasFilter))
    }

    def addChildToParent(parentAlias: String, cp: CoursePiece): Container =
      if (alias == parentAlias) setChilds(childs :+ cp)
      else setChilds(childs.map {
        case container: Container => container.addChildToParent(parentAlias, cp)
        case c => c
      })


    def moveUp(toMoveAlias: String): Container = {
      val id = childs.indexWhere(_.alias == toMoveAlias)

      val nc = if (id != -1 && id > 0 && childs.size >= 2)
        childs.updated(id, childs(id - 1)).updated(id - 1, childs(id))
      else childs

      setChilds(nc.map {
        case container: Container => container.moveUp(toMoveAlias)
        case c => c
      })
    }

    def moveDown(toMoveAlias: String): Container = {
      val id = childs.indexWhere(_.alias == toMoveAlias)

      val nc = if (id != -1 && id + 1 < childs.size && childs.size >= 2)
        childs.updated(id, childs(id + 1)).updated(id + 1, childs(id))
      else childs

      setChilds(nc.map {
        case container: Container => container.moveUp(toMoveAlias)
        case c => c
      })
    }

    def setChilds(c: Seq[CoursePiece]): Container
  }


  case class CourseRoot(title: String, annotation: String, childs: Seq[CoursePiece]) extends Container {
    def toJson: String = encodeRoot(this).noSpaces
    def toJsonPretty: String = encodeRoot(this).spaces2

    override def alias: String = "main"
    override def displayMe: DisplayMe = OwnPage

    override val displayInContentsHtml: Option[String] = Some(s"$title")

    override def setChilds(c: Seq[CoursePiece]): CourseRoot = this.copy(childs = c)
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

    override def setChilds(c: Seq[CoursePiece]): Theme = this.copy(childs = c)
  }

  case class SubTheme(
                       alias: String,
                       title: String,
                       textHtml: String = "",
                       childs: Seq[CoursePiece] = Seq(),
                       displayMe: DisplayMe = OwnPage
                     ) extends Container {
    override def setChilds(c: Seq[CoursePiece]): SubTheme = this.copy(childs = c)
    override val displayInContentsHtml: Option[String] = Some(s"$title")
    //    override def displayMe: DisplayMe = OwnPage
  }


  case class HtmlToDisplay(alias: String, displayMe: DisplayMe, htmlRaw: String) extends CoursePiece

  case class TextWithHeading(alias: String, heading: String, bodyHtml: String, displayMe: DisplayMe = Inline, override val displayInContentsHtml: Option[String] = None) extends CoursePiece

  // @Deprecated
  // case class ContainerWithHeading(alias: String, heading: String, bodyHtml: String, displayMe: DisplayMe = Inline,childs: Seq[CoursePiece] = Seq()) extends Container


  case class Paragraph(alias: String, bodyHtml: String, displayMe: DisplayMe = Inline) extends CoursePiece

  object Problem {
    def apply(pt: ProblemTemplate, displayMe: DisplayMe, inContents: Option[String] = None): Problem =
      Problem(pt.alias, displayMe, inContents)
  }

  case class Problem(problemAlias: String, displayMe: DisplayMe, override val displayInContentsHtml: Option[String]) extends CoursePiece {
    override def alias: String = problemAlias
  }


}

