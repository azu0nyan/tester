package tester.srv.controller


import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import zio.*

trait AdminService {

  def initCaches: TranzactIO[Unit]

  def addToAdmins(userId: Int): TranzactIO[Boolean]

  def removeFromAdmins(userId: Int): TranzactIO[Boolean]
  
  def isAdmin(userId: Int): UIO[Boolean]
}