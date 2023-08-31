package tester.srv.controller

import tester.srv.dao.CourseDao.Course
import tester.srv.dao.ProblemDao.Problem

trait CoursesService[F[_]]{
  def startCourseForUser(alias: String, userId: Int): F[Int]

  def stopCourse(alias: String, userId: Int): F[Unit]
  /** Так же удаляет все ответы пользователя */
  def removeCourseFromUser(alias: String, userId: Int): F[Unit]
  
  def courseProblems(courseId: Int): F[Seq[Problem]]

  def byId(courseId:Int):F [Course]
}

