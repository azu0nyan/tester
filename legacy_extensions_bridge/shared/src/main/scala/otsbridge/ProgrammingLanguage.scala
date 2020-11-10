package otsbridge

object ProgrammingLanguage {

  val ALL_LANGUAGES = Seq(Java, Scala, Cpp, Haskell)

  sealed trait ProgrammingLanguage
  case object Java extends ProgrammingLanguage
  case object Haskell extends ProgrammingLanguage
  case object Scala extends ProgrammingLanguage
  case object Cpp extends ProgrammingLanguage
}

