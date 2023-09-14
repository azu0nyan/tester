package tester.srv.controller.impl


import doobie.util.transactor
import doobie.*
import doobie.implicits.*
import io.github.gaelrenoux.tranzactio.DbException
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import tester.srv.controller.TeacherService
import tester.srv.dao.{AnswerTeacherDao, TeacherDao}
import zio.*
import zio.logging.*
import zio.concurrent.{ConcurrentMap, ConcurrentSet}

case class TeacherServiceImpl(
                               bus: MessageBus,
                               teachers: ConcurrentSet[Int],
                             //todo
//                               teacherToGroup: ConcurrentMap[Int, Set[Int]],
//                               groupToTeacher: ConcurrentMap[Int, Set[Int]],
                             ) extends TeacherService {
  override def initCaches(): TranzactIO[Unit] =
    for {
      ts <- TeacherDao.all
      _ <- ZIO.foreach(ts)(t => teachers.add(t.userId))
    } yield ()

  override def addToTeachers(userId: Int): TranzactIO[Boolean] = for {
    res <- TeacherDao.insert(TeacherDao.Teacher(userId))
    _ <- ZIO.when(res)(teachers.add(userId))
  } yield res

  override def removeFromTeachers(userId: Int): TranzactIO[Boolean] = for {
    res <- TeacherDao.deleteWhere(fr"userId = $userId")
  } yield res == 1

}


object TeacherServiceImpl {
  def live: URIO[MessageBus, TeacherService] =
    for {
      bus <- ZIO.service[MessageBus]
      set <- ConcurrentSet.make[Int]()
    } yield TeacherServiceImpl(bus, set)

  def layer = ZLayer.fromZIO(live)
}
