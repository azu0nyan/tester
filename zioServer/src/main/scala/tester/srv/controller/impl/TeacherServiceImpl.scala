package tester.srv.controller.impl


import doobie.util.transactor
import doobie.*
import doobie.implicits.*
import io.github.gaelrenoux.tranzactio.DbException
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import tester.srv.controller.TeacherService
import tester.srv.controller.MessageBus
import tester.srv.dao.TeacherToGroupDao.TeacherToGroup
import tester.srv.dao.{TeacherDao, TeacherToGroupDao}
import utils.ManyToManyRelation
import zio.*
import zio.logging.*
import zio.concurrent.{ConcurrentMap, ConcurrentSet}


case class TeacherServiceImpl(
                               bus: MessageBus,
                               teachers: ConcurrentSet[Int],
                               teacherToGroup: ManyToManyRelation[Int, Int]
                             ) extends TeacherService {
  override def initCaches: TranzactIO[Unit] =
    for {
      ts <- TeacherDao.all
      _ <- ZIO.logInfo(s"Caching ${ts.size} teachers")
      _ <- ZIO.foreach(ts)(t => teachers.add(t.userId))
      ttg <- TeacherToGroupDao.all
      _ <- ZIO.foreach(ttg)(tg => teacherToGroup.addXtoY(tg.teacherId, tg.groupId))
    } yield ()

  override def addToTeachers(userId: Int): TranzactIO[Boolean] = for {
    res <- TeacherDao.insert(TeacherDao.Teacher(userId))
    _ <- ZIO.when(res)(teachers.add(userId))
  } yield res

  override def removeFromTeachers(userId: Int): TranzactIO[Boolean] = for {
    res <- TeacherDao.deleteById(userId)
    _ <- ZIO.when(res)(teachers.remove(userId))
  } yield res

  def addTeacherToGroup(teacherId: Int, groupId: Int): TranzactIO[Boolean] = for {
    res <- TeacherToGroupDao.insert(TeacherToGroupDao.TeacherToGroup(teacherId, groupId))
    _ <- ZIO.when(res)(teacherToGroup.addXtoY(teacherId, groupId))
  } yield res

  def removeTeacherFromGroup(teacherId: Int, groupId: Int): TranzactIO[Boolean] = for {
    res <- TeacherToGroupDao.deleteWhere(fr"teacherID = $teacherId AND groupId = $groupId").map(_ == 1)
    _ <- ZIO.when(res)(teacherToGroup.removeXtoY(teacherId, groupId))
  } yield res


  def isTeacher(userId: Int): UIO[Boolean] = teachers.contains(userId)

  def teacherGroups(userId: Int): UIO[Set[Int]] = teacherToGroup.getX(userId)

}


object TeacherServiceImpl {
  def live: URIO[MessageBus, TeacherService] =
    for {
      bus <- ZIO.service[MessageBus]
      set <- ConcurrentSet.make[Int]()
      rel <- ManyToManyRelation.live[Int, Int]
    } yield TeacherServiceImpl(bus, set, rel)

  def layer = ZLayer.fromZIO(live)
}
