package otsbridge


//case class ProgramAnswer(program: String, language: ProgrammingLanguage)

sealed trait AnswerField {
  type Answer
  val questionText: String
  def answerToString(answer: Answer): String = answer.toString
  def answerFromString(string: String): Option[Answer]
}
case class DoubleNumberField(override val questionText: String) extends AnswerField {
  override type Answer = Double
  override def answerFromString(string: String): Option[Double] = string.toDoubleOption
}
case class IntNumberField(override val questionText: String) extends AnswerField {
  override type Answer = Int
  override def answerFromString(string: String): Option[Int] = string.toIntOption
}
case class TextField(override val questionText: String) extends AnswerField {
  override type Answer = String
  override def answerFromString(string: String): Option[String] = Some(string)
}

case class ProgramInTextField(override val questionText: String,
                              programmingLanguage: ProgrammingLanguage,
                              initialProgram:Option[String]) extends AnswerField {
  override type Answer = String
  override def answerFromString(string: String): Option[String] = Some(string)
}

case class SelectOneField(override val questionText: String, variants: Set[String]) extends AnswerField {
  override type Answer = String
  override def answerFromString(string: String): Option[String] = ???
}

case class SelectManyField(override val questionText: String, variants: Set[String]) extends AnswerField {
  override type Answer = Set[String]
  override def answerFromString(string: String): Option[Set[String]] = ???
}

//case class CompoundAnswerField(override val questionText: String, seq: Seq[AnswerField[_]])
//  extends AnswerField[Seq[_]] {
//  override def answerFromString(string: _root_.scala.Predef.String): _root_.scala.Option[scala.Seq[_]] = ???
//}




