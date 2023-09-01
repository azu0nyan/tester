package tester.srv.controller

import tester.srv.dao.CourseDao.Course
import tester.srv.dao.ProblemDao.Problem
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
trait CoursesService {
  def startCourseForUser(alias: String, userId: Int): TranzactIO[Int]

  def stopCourse(alias: String, userId: Int): TranzactIO[Unit]
  /** Так же удаляет все ответы пользователя */
  def removeCourseFromUser(alias: String, userId: Int): TranzactIO[Unit]

  def courseProblems(courseId: Int): TranzactIO[Seq[Problem]]

  def byId(courseId: Int): TranzactIO[Course]

  def courseViewData(courseId: Int): TranzactIO[viewData.CourseViewData]

  def partialCourseViewData(courseId: Int): TranzactIO[viewData.PartialCourseViewData]

  def userCourses(userId: Int): TranzactIO[Seq[viewData.CourseViewData]]

}

