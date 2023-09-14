package tester.srv.controller



import io.github.gaelrenoux.tranzactio.doobie.TranzactIO

trait TeacherService {
  
  def initCaches(): TranzactIO[Unit]

  def addToTeachers(userId: Int): TranzactIO[Boolean]

  def removeFromTeachers(userId: Int): TranzactIO[Boolean]
  
  

}
