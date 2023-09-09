package dbMigration

import DbViewsShared.CourseShared
import controller.UserRole
import controller.db.{CollectionOps, answers}

import java.util.concurrent.atomic.AtomicInteger
import controller.UserRole._
import dbMigration.DbJsonMappings._
import io.getquill._

import java.io.File
import java.time.{Instant, LocalDateTime, ZoneOffset}

object Main {
  //  case class RegisteredUser(id: Int, login: String, passwordHash: String, passwordSalt: String, firstName: Option[String], lastName: Option[String], email: Option[String], registeredAt: java.time.LocalDateTime, lastLogin: Option[java.time.LocalDateTime], role: String)

  import io.circe.syntax._
  import io.circe._
  import io.circe.generic.semiauto._
  import io.circe.parser.decode


  import io.circe.generic.auto._
  import io.circe._
  import io.circe.generic.semiauto._
  import io.circe.syntax._


  import dbMigration.DbModel._

  val ctx = new PostgresJdbcContext[CamelCase](CamelCase, "databaseConfig")


  import ctx._

  def main(args: Array[String]): Unit = try {
    migrateUsers()
    migrateGroups()
    migrateUserToGroup()
    migrateCourses()
    migrateCourseTemplateForGroup()
    migrateCustomProblems()
    migrateCustomCourses()
    migrateProblems()
    migrateAnswers()


  } catch {
    case t: Throwable =>
      t.printStackTrace()
  } finally {
    ctx.close()
  }

  var userIds: Map[String, Int] = Map()
  var groupIds: Map[String, Int] = Map()
  var courseIds: Map[String, Int] = Map()
  var problemIds: Map[String, Int] = Map()


  def migrateCustomProblems(): Unit = {
    val cpt = controller.db.customProblemTemplates.all()
    for (t <- cpt) {
      val toIns = CustomProblemTemplate(t.uniqueAlias, t.staticTitle, t.staticHtml,
        t.staticAnswerField.asJson.noSpaces, t.initialScore.asJson.noSpaces)
      val q = quote { (p: CustomProblemTemplate) =>
        sql"""INSERT INTO ProblemTemplate (alias, title, html, answerField, initialScore)
             VALUES (${p.alias}, ${p.title}, ${p.html}, ${p.answerField}::jsonb, ${p.initialScore}::jsonb)"""
          .as[Insert[CustomProblemTemplate]].onConflictIgnore
      }
      ctx.run(q(ctx.lift(toIns)))

    }
  }

  def migrateCustomCourses(): Unit = {
    val ids = new AtomicInteger(0)
    val acc = controller.db.customCourseTemplates.all()
    val accIds = acc.map(u => (u._id.toHexString, ids.getAndIncrement())).toMap
    for (c <- acc) {
      val toIns = CustomCourseTemplate(c.uniqueAlias, c.description, c.courseData.asJson.noSpaces)

      val q = quote { (c: CustomCourseTemplate) =>
        sql"""INSERT INTO CourseTemplate (templateAlias, description, courseData)
             VALUES ( ${c.templateAlias}, ${c.description}, ${c.courseData}::json)"""
          .as[Insert[CustomCourseTemplate]].onConflictIgnore
      }


//      ctx.run(q(ctx.lift(toIns))) todo

      for (p <- c.problemAliasesToGenerate) {
        val toIns2 = CourseTemplateProblem(toIns.templateAlias, p)

        val q1 = quote {
          query[CourseTemplateProblem].insertValue(ctx.lift(toIns2))
        }
        ctx.run(q1)
      }
    }
  }

  def migrateGroups(): Unit = {
    val ids = new AtomicInteger(0)
    val allGroups = controller.db.groups.all()
    println(s"Found ${allGroups.size} groups.")
    groupIds = allGroups.map(u => (u._id.toHexString, ids.getAndIncrement())).toMap

    for (g <- allGroups) {
      val toIns = UserGroup(groupIds(g._id.toHexString), g.title, g.description)
      val q = quote {
        query[UserGroup].insertValue(ctx.lift(toIns)).onConflictIgnore
      }
      ctx.run(q)
    }
  }

  def migrateCourses(): Unit = {
    val ids = new AtomicInteger(0)
    val allCourses = controller.db.courses.all()
    println(s"Found ${allCourses.size} courses.")
    courseIds = allCourses.map(u => (u._id.toHexString, ids.getAndIncrement())).toMap

    for (c <- allCourses) {
      val toIns = Course(courseIds(c._id.toHexString), userIds(c.userId.toHexString), c.templateAlias, c.seed)
      val q = quote {
        query[Course].insertValue(ctx.lift(toIns)).onConflictIgnore
      }
      ctx.run(q)
    }
  }

  def migrateProblems(): Unit = {
    val ids = new AtomicInteger(0)
    val allProblems = controller.db.problems.all()
    println(s"Found ${allProblems.size} problems.")
    problemIds = allProblems.map(u => (u._id.toHexString, ids.getAndIncrement())).toMap
    var c = 0

    for (p <- allProblems) try{
      val toIns = Problem(problemIds(p._id.toHexString), courseIds(p.courseId.toHexString), p.templateAlias, p.seed,
        p.score.percentage,  p.score.asJson.noSpaces, LocalDateTime.now())

      c += 1
      if(c % 1000 == 0) {
        println(s"Problem id $c")
      }

      val q = quote { (p: Problem) =>
        sql"""INSERT INTO Problem (id,courseId,templateAlias,seed,score)
             VALUES (${p.id}, ${p.courseId}, ${p.templateAlias}, ${p.seed}, ${p.score}::jsonb)"""
          .as[Insert[Problem]].onConflictIgnore
      }
      ctx.run(q(ctx.lift(toIns)))
    } catch {
      case t: Throwable =>
        println(s"Error while migrating problem ${p}")
        t.printStackTrace()
    }
  }

  def migrateAnswers(): Unit = {
    val as = controller.db.answers.all()
    val ids = new AtomicInteger(0)
    println(s"Found ${as.size} answers")

    for (a <- as) try{
      val id = ids.getAndIncrement()
      if (ids.get() % 1000 == 0) {
        println(s"answer ${ids.get()}")
      }
      val toIns = Answer(id, problemIds(a.problemId.toHexString), a.answer,  LocalDateTime.ofInstant(a.answeredAt, ZoneOffset.UTC))
      val q = quote { (a: Answer) =>
        sql"""INSERT INTO Answer (id, problemId, answer, answeredAt)
             VALUES (${a.id}, ${a.problemId}, ${a.answer}, ${a.answeredAt})"""
          .as[Insert[Answer]].onConflictIgnore
      }
      ctx.run(q(ctx.lift(toIns)))


      case class AnswerRejection(answerId: Int,  rejectedAt: Instant, message: Option[String], rejectedBy: Option[Int])
      case class AnswerReview(answerId: Int,  text: String, reviewerId: Int)
      case class AnswerVerification(answerId: Int, verifiedAt: Instant, systemMessage: Option[String], score: String, scoreNormalized: Double)
      case class answerverifiactionconfirmation(answerId: Int,  confirmedAt: Instant, confirmedById: Option[Int])


      a.status match {
        case CourseShared.VerifiedAwaitingConfirmation(score, systemMessage, verifiedAt) =>
          val q = quote { (a: AnswerVerification) =>
            sql"""INSERT INTO AnswerVerification (answerId, verifiedAt, systemMessage, score, scoreNormalized)
                 VALUES (${a.answerId}, ${a.verifiedAt}, ${a.systemMessage}, ${a.score}::jsonb, ${a.scoreNormalized})"""
              .as[Insert[AnswerVerification]].onConflictIgnore
          }
          val verification = AnswerVerification(id, verifiedAt, systemMessage, score.asJson.noSpaces, score.percentage)
          ctx.run(q(ctx.lift(verification)))

        case CourseShared.Verified(score, review, systemMessage, verifiedAt, confirmedAt) =>
          val q = quote { (a: AnswerVerification) =>
            sql"""INSERT INTO AnswerVerification (answerId, verifiedAt, systemMessage, score, scoreNormalized)
                 VALUES (${a.answerId}, ${a.verifiedAt}, ${a.systemMessage}, ${a.score}::jsonb, ${a.scoreNormalized})"""
              .as[Insert[AnswerVerification]].onConflictIgnore
          }
          val verification = AnswerVerification(id, verifiedAt, systemMessage, score.asJson.noSpaces, score.percentage)
          ctx.run(q(ctx.lift(verification)))

          val qq = quote {
            query[answerverifiactionconfirmation].insertValue(ctx.lift(answerverifiactionconfirmation(id, confirmedAt.getOrElse(java.time.Clock.systemUTC().instant()), None)))
          }
          ctx.run(qq)
          if(review.nonEmpty){
            val qqq = quote {
              query[AnswerReview].insertValue(ctx.lift(AnswerReview(id, review.get, 0)))
            }
            ctx.run(qqq)
          }


        case CourseShared.Rejected(systemMessage, rejectedAt) =>
          val q = quote {
            query[AnswerRejection].insertValue(ctx.lift(AnswerRejection(id, rejectedAt, systemMessage, None)))
          }
          ctx.run(q)
        case CourseShared.BeingVerified() =>
        case CourseShared.VerificationDelayed(systemMessage) =>
          val q = quote{
            query[AnswerRejection].insertValue(ctx.lift(AnswerRejection(id, a.answeredAt, systemMessage, None)))
          }
          ctx.run(q)
      }

    } catch {
      case t: Throwable =>
        println(s"Error while migrating answer ${a._id}")
        t.printStackTrace()

    }
  }

  def migrateCourseTemplateForGroup(): Unit = {
    val templates = controller.db.courseTemplateForGroup.all()
    for (t <- templates) {
      val toIns = CourseTemplateForGroup(0, groupIds(t.groupId.toHexString), t.templateAlias, t.forceStartForGroupMembers)
      val q = quote {
        query[CourseTemplateForGroup].insertValue(ctx.lift(toIns)).onConflictIgnore.returningGenerated(_.id)
      }
      ctx.run(q)
    }
  }

  def migrateUserToGroup(): Unit = {
    val utg = controller.db.userToGroup.all()
    for (ut <- utg) {
      val toIns = UserToGroup(0, userIds(ut.userId.toHexString), groupIds(ut.groupId.toHexString), LocalDateTime.now(), None)
      val q = quote {
        query[UserToGroup].insertValue(ctx.lift(toIns)).onConflictIgnore.returningGenerated(_.id)
      }
      ctx.run(q)
    }
  }

  def migrateUsers(): Unit = {
    val uids = new AtomicInteger(0)
    val allUsers = controller.db.users.all()
    println(s"Found ${allUsers.size} users.")

    userIds = allUsers.map(u => (u._id.toHexString, uids.getAndIncrement())).toMap

    for (u <- allUsers) {
      println(u)
      val toIns = RegisteredUser(userIds(u._id.toHexString), u.login, u.passwordHash, u.passwordSalt,
        u.firstName.getOrElse(""), u.lastName.getOrElse(""), u.email.getOrElse(""),
        LocalDateTime.ofInstant(u.registeredAt.getOrElse(Instant.now()), ZoneOffset.UTC),
        u.lastLogin.map(l => LocalDateTime.ofInstant(l, ZoneOffset.UTC)))
      /*
            val q = quote {
              query[RegisteredUser].insertValue(ctx.lift(toIns))
            } //?::jsonb
      */

      val req =
        quote { (ru: RegisteredUser) =>
          sql"""INSERT INTO RegisteredUser (id,login,passwordHash,passwordSalt,firstName,lastName,email,registeredAt,lastLogin)
               VALUES (${ru.id}, ${ru.login}, ${ru.passwordHash}, ${ru.passwordSalt}, ${ru.firstName}, ${ru.lastName}, ${ru.email},
               ${ru.registeredAt}, ${ru.lastLogin})"""
            .as[Insert[RegisteredUser]].onConflictIgnore
        }

      val qr = req

      ctx.run(qr(lift(toIns)))
    }


  }
}
