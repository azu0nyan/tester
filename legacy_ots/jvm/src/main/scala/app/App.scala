package app

import controller.{Maintenance, TemplatesRegistry}
import impl.BinaryCountingOfAncientRussians

object App {

  def main(args: Array[String]): Unit = {

    initAliases()

    if(args.length == 1 && (args(0) == "-h" || args(0) == "-help")) {
      println("-snp start new problems for course")
      println("-ruwg removeUsersWithoutGroups")
      println("-rawp removeAnswersWithoutProblems")
      println("-rpwc removeProblemsWithoutCourse")
    }

    for(a <- args) a match {
      case "-snp" | "--startNewProblem" => Maintenance.findAndFixNonStartedProblem()
      case "-ruwg" | "--removeUsersWithoutGroups" => Maintenance.removeUsersWOGroups()
      case "-rawp" | "--removeAnswersWithoutProblems" => Maintenance.removeAnswersWoProblems()
      case "-rpwc" | "--removeProblemsWithoutCourse" => Maintenance.removeProblemsWOCourse()
      case _ => println(s"Unknown parameter $a")
    }


    HttpServer.initRoutesAndStart()
  }

  def initAliases(): Unit = {
//    TemplatesRegistry.registerOrUpdateCourseTemplate(BinaryCountingOfAncientRussians.template)
    TemplatesRegistry.registerDataPack(courses.javaCourse.data)
    TemplatesRegistry.registerDataPack(courses.datastructures.data)
    TemplatesRegistry.registerDataPack(courses.algos.data)
    TemplatesRegistry.registerDataPack(myCourses.g7_20_21.data)
    TemplatesRegistry.registerDataPack(myCourses.g8_20_21.data)
    TemplatesRegistry.registerDataPack(myCourses.g8i_20_21.data)
    TemplatesRegistry.registerDataPack(myCourses.g9_20_21.data)
    TemplatesRegistry.registerDataPack(myCourses.g11_20_21.data)

    //    TemplatesRegistry.registerOrUpdateCourseTemplate(tasks.javaCourse.JavaCourse)
  }


}
