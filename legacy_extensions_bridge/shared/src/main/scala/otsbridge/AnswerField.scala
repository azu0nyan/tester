package otsbridge

import io.circe._
import io.circe.parser._
import io.circe.generic.auto._
import io.circe.syntax._
import otsbridge.ProgrammingLanguage

//case class ProgramAnswer(program: String, language: ProgrammingLanguage)
object AnswerField {

  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val resDec: Decoder[AnswerField] = deriveDecoder[AnswerField]
  implicit val resEnc: Encoder[AnswerField] = deriveEncoder[AnswerField]
  implicit val resDec1: Decoder[ProgramAnswer] = deriveDecoder[ProgramAnswer]
  implicit val resEnc1: Encoder[ProgramAnswer] = deriveEncoder[ProgramAnswer]

  def fromJson(af: String): AnswerField = decode[AnswerField](af) match
    case Left(value) => ???
    case Right(value) => value

  sealed trait AnswerField {
    type Answer
    val questionText: String
    def answerToString(answer: Answer): String = answer.toString
    def answerFromString(string: String): Option[Answer]
    def toJson: String = this.asJson(resEnc).noSpaces
  }
  case class DoubleNumberField(override val questionText: String) extends AnswerField {
    override type Answer = Double
    override def answerFromString(string: String): Option[Double] = string.toDoubleOption
  }
  case class IntNumberField(override val questionText: String) extends AnswerField {
    override type Answer = Int
    override def answerFromString(string: String): Option[Int] = string.toIntOption
  }
  case class TextField(override val questionText: String, lines: Int = 1) extends AnswerField {
    override type Answer = String
    override def answerFromString(string: String): Option[String] = Some(string)
  }

  case class ProgramAnswer(program: String, programmingLanguage: ProgrammingLanguage) {
    def toJson: String = this.asJson.noSpaces
  }
  object ProgramAnswer {
    def fromJsom(a: String): ProgramAnswer = decode[ProgramAnswer](a) match
      case Left(value) => ???
      case Right(value) => value
  }

  case class ProgramInTextField(override val questionText: String,
                                allowedLanguages: Seq[ProgrammingLanguage],
                                initialProgram: Option[String]) extends AnswerField {
    override type Answer = ProgramAnswer
    override def answerFromString(string: String): Option[ProgramAnswer] = decode[ProgramAnswer](string).toOption
      .filter(x => allowedLanguages.contains(x.programmingLanguage))

    override def answerToString(answer: Answer): String = answer.asJson.noSpaces

  }

  case class SelectOneField(override val questionText: String, variants: Set[String]) extends AnswerField {
    override type Answer = String
    override def answerFromString(string: String): Option[String] = ???
  }

  case class SelectManyField(override val questionText: String, variants: Set[String]) extends AnswerField {
    override type Answer = Set[String]
    override def answerFromString(string: String): Option[Set[String]] = ???
  }
}


//case class CompoundAnswerField(override val questionText: String, seq: Seq[AnswerField[_]])
//  extends AnswerField[Seq[_]] {
//  override def answerFromString(string: _root_.scala.Predef.String): _root_.scala.Option[scala.Seq[_]] = ???
//}




