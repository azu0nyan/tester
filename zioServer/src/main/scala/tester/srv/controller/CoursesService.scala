package tester.srv.controller

import tester.srv.dao.CourseDao.Course
import tester.srv.dao.ProblemDao.Problem
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import viewData.PartialCourseViewData
trait CoursesService {
  def startCourseForUser(alias: String, userId: Int): TranzactIO[Int]

  def stopCourse(alias: String, userId: Int): TranzactIO[Unit]
  /** Так же удаляет все ответы пользователя */
  def removeCourseFromUser(alias: String, userId: Int): TranzactIO[Unit]

  def courseProblems(courseId: Int): TranzactIO[Seq[Problem]]

  def byId(courseId: Int): TranzactIO[Option[Course]]

  def courseViewData(courseId: Int): TranzactIO[Option[viewData.CourseViewData]]

  def partialCourseViewData(courseId: Int): TranzactIO[Option[PartialCourseViewData]]

  def userCourses(userId: Int): TranzactIO[Seq[viewData.CourseViewData]]

}

