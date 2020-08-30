package app

import controller.TemplatesRegistry
import impl.BinaryCountingOfAncientRussians
import tasks.javaCourse.JavaCourse

object App {

  def main(args: Array[String]): Unit = {
    initAliases()
    HttpServer.initRoutesAndStart()
  }

  def initAliases(): Unit = {
    TemplatesRegistry.registerOrUpdateCourseTemplate(BinaryCountingOfAncientRussians.template)
    TemplatesRegistry.registerDataPack(tasks.javaCourse.data)
//    TemplatesRegistry.registerOrUpdateCourseTemplate(tasks.javaCourse.JavaCourse)
  }


}
