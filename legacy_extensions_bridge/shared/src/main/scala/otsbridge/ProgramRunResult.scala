package otsbridge

object ProgramRunResult {
  //  sealed trait  ProgramRunResult2
  //  case class ProgramRunResultSuccess2(timeMS: Long, message:Option[String]) extends ProgramRunResult2
  //  case class ProgramRunResultWrongAnswer2(timeMS: Long, message:Option[String]) extends ProgramRunResult2
  //  case class ProgramRunResultFailure2() extends ProgramRunResult2
  sealed trait ProgramRunResult
  case class ProgramRunResultSuccess(timeMS: Long, message: Option[String]) extends ProgramRunResult
  case class ProgramRunResultWrongAnswer(message: Option[String]) extends ProgramRunResult
  case class ProgramRunResultFailure(message: Option[String]) extends ProgramRunResult

}



