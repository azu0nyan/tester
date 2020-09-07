package otsbridge


//TODO undeprecate
@Deprecated
sealed trait CourseType{
}
@Deprecated
object CourseType{

  case class SimpleCourse() extends CourseType
  case class WithTimeLimit() extends CourseType
}
