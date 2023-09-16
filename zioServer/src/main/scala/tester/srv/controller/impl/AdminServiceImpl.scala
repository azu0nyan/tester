package tester.srv.controller.impl

import doobie.implicits.*
import doobie.util.transactor
import io.github.gaelrenoux.tranzactio.DbException
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import tester.srv.controller.{AdminService, MessageBus}
import tester.srv.dao.AdminDao
import utils.ManyToManyRelation
import zio.{Task, UIO, URIO, ZIO, ZLayer}
import zio.concurrent.ConcurrentSet

case class AdminServiceImpl(
                             bus: MessageBus,
                             admins: ConcurrentSet[Int],
                           ) extends AdminService {
  override def initCaches: TranzactIO[Unit] =
    for {
      adms <- AdminDao.all
      _ <- ZIO.logInfo(s"Caching ${adms.size} admins")
      _ <- ZIO.foreach(adms)(adm => admins.add(adm.userId))
    } yield ()

  override def addToAdmins(userId: Int): TranzactIO[Boolean] = for {
    res <- AdminDao.insert(AdminDao.Admin(userId))
    _ <- ZIO.when(res)(admins.add(userId))
  } yield res

  override def removeFromAdmins(userId: Int): TranzactIO[Boolean] = for {
    res <- AdminDao.deleteById(userId)
    _ <- ZIO.when(res)(admins.remove(userId))
  } yield res

  def isAdmin(userId: Int): UIO[Boolean] = admins.contains(userId)
}

object AdminServiceImpl {
  def live: URIO[MessageBus, AdminService] =
    for {
      bus <- ZIO.service[MessageBus]
      set <- ConcurrentSet.make[Int]()
    } yield AdminServiceImpl(bus, set)

  def layer = ZLayer.fromZIO(live)
}