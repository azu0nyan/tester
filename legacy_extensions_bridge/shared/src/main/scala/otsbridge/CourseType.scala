package otsbridge

sealed trait CourseType{
}

object CourseType{

  case class SimpleCourse() extends CourseType
  case class WithTimeLimit() extends CourseType //todo
}
