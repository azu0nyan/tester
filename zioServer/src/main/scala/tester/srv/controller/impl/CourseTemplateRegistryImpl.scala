package tester.srv.controller.impl

import otsbridge.CourseTemplate
import tester.srv.controller.{CourseTemplateRegistry, ProblemInfoRegistry}
import zio.*
import zio.concurrent.ConcurrentMap

case class CourseTemplateRegistryImpl(map: ConcurrentMap[String, CourseTemplate]) extends CourseTemplateRegistry {
  def courseTemplate(alias: String): UIO[Option[CourseTemplate]] = map.get(alias)
  def registerCourseTemplate(template: CourseTemplate): UIO[Unit] = map.put(template.uniqueAlias, template).map(_ => ())
}

object CourseTemplateRegistryImpl {
  def live: UIO[CourseTemplateRegistry] =
    for {
      map <- ConcurrentMap.make[String, CourseTemplate]()
    } yield CourseTemplateRegistryImpl(map)

  def layer: ULayer[CourseTemplateRegistry] = ZLayer.fromZIO(live)
}
