package tester.srv.controller

trait Courses[F[_]]{
  def startCourseForUser(alias: String, userId: Int): F[Int]

  def stopCourse(alias: String, userId: Int): F[Unit]

  /** Так же удаляет все ответы пользователя */
  def removeCourseFromUser(alias: String, userId: Int): F[Unit]
}

