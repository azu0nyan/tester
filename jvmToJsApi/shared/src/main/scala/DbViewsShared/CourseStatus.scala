package DbViewsShared

import java.time.Instant

sealed trait CourseStatus
object CourseStatus {
  case class Passing(endsAt: Option[Instant]) extends CourseStatus
  case class Finished(/*score: Option[ProblemListScore]*/) extends CourseStatus
}

