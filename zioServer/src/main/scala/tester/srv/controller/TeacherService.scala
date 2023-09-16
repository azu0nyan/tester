package tester.srv.controller


import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import zio.*

trait TeacherService {

  def initCaches: TranzactIO[Unit]
  
  def isTeacher(userId: Int): UIO[Boolean]
  
  def teacherGroups(userId: Int): UIO[Set[Int]]

  def addToTeachers(userId: Int): TranzactIO[Boolean]

  def removeFromTeachers(userId: Int): TranzactIO[Boolean]

  def addTeacherToGroup(teacherId: Int, groupId: Int): TranzactIO[Boolean]

  def removeTeacherFromGroup(teacherId: Int, groupId: Int): TranzactIO[Boolean]

}
