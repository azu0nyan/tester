package tester.srv.controller

import otsbridge.ProblemScore.ProblemScore
import zio.*
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import otsbridge.ProblemInfo
import viewData.ProblemViewData

trait ProblemService {

  /** Returns problem id */
  def startProblem(courseId: Int, templateAlias: String): TranzactIO[Int]

  def removeProblem(courseId: Int, templateAlias: String): TranzactIO[Boolean]
  
  def reportAnswerConfirmed(problemId: Int, asnwerId:Int, score: ProblemScore): TranzactIO[Unit]
  
  def setScore(problemId: Int,  score: ProblemScore): TranzactIO[Boolean]

  def getRefViewData(problemId: Int): TranzactIO[viewData.ProblemRefViewData]
  
  def getViewData(problemId: Int): TranzactIO[ProblemViewData]
  
  def registerInfo(info: ProblemInfo): UIO[Unit]
}
