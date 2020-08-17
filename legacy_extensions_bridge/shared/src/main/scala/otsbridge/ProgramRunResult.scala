package otsbridge

sealed trait ProgramRunResult
case class ProgramRunResultSuccess(timeMS: Long, message:Option[String]) extends ProgramRunResult
case class ProgramRunResultWrongAnswer(timeMS: Long, message:Option[String]) extends ProgramRunResult
case class ProgramRunResultFailure(message:Option[String]) extends ProgramRunResult
