package tester.srv.controller.impl

import doobie.util.transactor
import io.github.gaelrenoux.tranzactio.DbException
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import tester.srv.controller.{AdminService, MessageBus, TeacherService}
import tester.srv.dao.{TeacherDao, TeacherToGroupDao}
import utils.ManyToManyRelation
import zio.{Task, URIO, ZIO, ZLayer}
import zio.concurrent.ConcurrentSet

case class AdminServiceImpl(
                               bus: MessageBus,
                               admins: ConcurrentSet[Int],
                             ) extends AdminService {
  override def initCaches: TranzactIO[Unit] =
    for {
      _ <- admins.add(0) //todo
    } yield ()

  //  override def addToAdmins(userId: Int): TranzactIO[Boolean] = for {
  //    res <- TeacherDao.insert(TeacherDao.Teacher(userId))
  //    _ <- ZIO.when(res)(teachers.add(userId))
  //  } yield res
  //
  //  override def removeFromTeachers(userId: Int): TranzactIO[Boolean] = for {
  //    res <- TeacherDao.deleteWhere(fr"userId = $userId").map(_ == 1)
  //    _ <- ZIO.when(res)(teachers.remove(userId))
  //  } yield res
  override def addToAdmins(userId: Int): TranzactIO[Boolean]  = ???
  override def removeFromAdmins(userId: Int): TranzactIO[Boolean]  = ???
}

object AdminServiceImpl {
  def live: URIO[MessageBus, AdminService] =
    for {
      bus <- ZIO.service[MessageBus]
      set <- ConcurrentSet.make[Int]()
    } yield AdminServiceImpl(bus, set)

  def layer = ZLayer.fromZIO(live)
}