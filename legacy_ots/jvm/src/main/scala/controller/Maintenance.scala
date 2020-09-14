package controller

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

object Maintenance {

  val log: Logger = Logger(LoggerFactory.getLogger("controller.Maintenance"))

  def findAndFixNonStartedProblem() : Unit= {
    log.info(s"Checking courses")
      for(c <- db.courses.all()){
        log.info(s"Checking course ${c.idAlias}")
        val ct = TemplatesRegistry.getCourseTemplate(c.templateAlias)
        ct match {
          case Some(ct) =>
            val aliasesExisting = c.ownProblems.map(_.templateAlias).toSet
            val aliasesShouldBe = ct.problemAliasesToGenerate.toSet
            val aliasesToGenerate = aliasesShouldBe &~ aliasesExisting
            if(aliasesToGenerate.nonEmpty){
              log.info(s"Found problems to generate ${aliasesToGenerate.size}")
              for(a <- aliasesToGenerate){
                TemplatesRegistry.getProblemTemplate(a) match {
                  case Some(template) => Generator.addProblemToCourse(template, c)
                  case None =>log.error(s"Cant find problem template by alias $a. Course ${c.idAlias}. User ${c.user.idAndLoginStr}")
                }
              }
            }

          case None =>
            log.error(s"Cant find course template by alias. Course ${c.idAlias}. User ${c.user.idAndLoginStr}")
        }

      }
  }


  def removeUsersWOGroups() :Unit = {
    val toRemove = db.users.all().filter(_.groups.isEmpty)
    log.info(s"Found ${toRemove.size} without groups. Removing...")
    toRemove.foreach(UserOps.deleteUser)
  }

  def removeUsersWOCourses():Unit = {
    val toRemove = db.users.all().filter(_.courses.isEmpty)
    log.info(s"Found ${toRemove.size} users without courses. Removing...")
    toRemove.foreach(UserOps.deleteUser)
  }

  def removeAnswersWoProblems():Unit = {
    val toRemove = db.answers.all().filter(a => db.problems.byId(a.problemId).isEmpty)
    log.info(s"Found ${toRemove.size} answers without problems. Removing...")
    toRemove.foreach(AnswerOps.deleteAnswer)
  }

  def removeProblemsWOCourse():Unit = {
    val toRemove = db.problems.all().filter(p => db.courses.byId(p.courseId).isEmpty)
    log.info(s"Found ${toRemove.size} problems wothout course. Removing...")
    toRemove.foreach(ProblemOps.removeProblem)
  }

  def findAndRecheckInterruptedProblems() : Unit = {
    //todo
  }

}
