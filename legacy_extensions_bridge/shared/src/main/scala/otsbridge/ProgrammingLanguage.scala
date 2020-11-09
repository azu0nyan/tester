package otsbridge

object ProgrammingLanguage {

  val ALL_LANGUAGES = Seq(Java, Scala, Cpp)

  sealed trait ProgrammingLanguage
  case object Java extends ProgrammingLanguage
  case object Scala extends ProgrammingLanguage
  case object Cpp extends ProgrammingLanguage
}

