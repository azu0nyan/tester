package tester.srv.controller

import otsbridge.{CourseTemplate, ProblemInfo, ProblemTemplate}
import zio.UIO


trait CourseTemplateRegistry {
    def courseTemplate(alias: String): UIO[Option[CourseTemplate]]
    def registerCourseTemplate(template: CourseTemplate): UIO[Unit]  
}
