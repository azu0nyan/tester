package otsbridge

sealed trait ProgrammingLanguage

object ProgrammingLanguage {
  
  val ALL_LANGUAGES = Seq(Java, Scala, Cpp, Haskell, Kojo)
  val KOJO_FIRST = Seq(Kojo, Java, Scala, Cpp, Haskell )
  
  case object Java extends ProgrammingLanguage
  case object Haskell extends ProgrammingLanguage
  case object Scala extends ProgrammingLanguage
  case object Kojo extends ProgrammingLanguage
  case object Cpp extends ProgrammingLanguage
}

