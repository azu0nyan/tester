package dbMigration

import controller.UserRole
import controller.db.CollectionOps

import java.util.concurrent.atomic.AtomicInteger
import controller.UserRole._

import dbMigration.DbJsonMappings._
import io.getquill._
import io.getquill.context.json.PostgresJsonExtensions

import java.io.File
import java.time.{Instant, LocalDateTime, ZoneOffset}

object Main {
  //  case class RegisteredUser(id: Int, login: String, passwordHash: String, passwordSalt: String, firstName: Option[String], lastName: Option[String], email: Option[String], registeredAt: java.time.LocalDateTime, lastLogin: Option[java.time.LocalDateTime], role: String)

  import dbMigration.DbModel._

  val ctx = new PostgresJdbcContext[PostgresEscape](PostgresEscape, "databaseConfig") with PostgresJsonExtensions

  import ctx._

  def main(args: Array[String]): Unit = try {
    migrateUsers()
    migrateGroups()
    migrateUserToGroup()
    migrateCourses()
    migrateCourseTemplateForGroup()
    migrateProblems()
    migrateAnswers()
    migrateCustomProblems()
    migrateCustomCourses()


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
      val toIns = CustomProblemTemplate(t.uniqueAlias, t.staticTitle, t.staticHtml, JsonbValue(t.staticAnswerField), JsonbValue(t.initialScore))
      val q = quote { (p: CustomProblemTemplate) =>
        sql"""INSERT INTO "CustomProblemTemplate" ("alias", "title", "html", "answerField", "initialScore")
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
      val toIns = CustomCourseTemplate(accIds(c._id.toHexString), c.uniqueAlias, c.description, JsonbValue(c.courseData))

      val q = quote { (c: CustomCourseTemplate) =>
        sql"""INSERT INTO "CustomCourseTemplate" ("id", "templateAlias", "description","courseData")
             VALUES (${c.id}, ${c.templateAlias}, ${c.description}, ${c.courseData}::jsonb)"""
          .as[Insert[CustomCourseTemplate]].onConflictIgnore
      }
      ctx.run(q(ctx.lift(toIns)))

      for (p <- c.problemAliasesToGenerate) {
        val toIns2 = CustomCourseTemplateProblemAlias(toIns.id, p)

        val q1 = quote {
          query[CustomCourseTemplateProblemAlias].insertValue(ctx.lift(toIns2))
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
      println(g)
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
      println(c)
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
      val toIns = Problem(problemIds(p._id.toHexString), courseIds(p.courseId.toHexString), p.templateAlias, p.seed, JsonbValue(p.score))

      c += 1
      if(c % 1000 == 0) println(c)

      val q = quote { (p: Problem) =>
        sql"""INSERT INTO "Problem" ("id","courseId","templateAlias","seed","score")
             VALUES (${p.id}, ${p.courseId}, ${p.templateAlias}, ${p.seed}, ${p.score}::jsonb)"""
          .as[Insert[Problem]].onConflictIgnore
      }
      ctx.run(q(ctx.lift(toIns)))
    } catch {
      case t: Throwable =>
        println(s"Error while migrating problem ${p}")
    }
  }

  def migrateAnswers(): Unit = {
    val as = controller.db.answers.all()
    val ids = new AtomicInteger(0)
    println(s"Found ${as.size} answers")

    for (a <- as) try{
      ids.getAndIncrement()
      if (ids.get() % 1000 == 0) {
        println(ids.get())
      }
      val toIns = Answer(0, problemIds(a.problemId.toHexString), a.answer, JsonbValue(a.status), LocalDateTime.ofInstant(a.answeredAt, ZoneOffset.UTC))
      val q = quote { (a: Answer) =>
        sql"""INSERT INTO "Answer" ("problemId", "answer", "status", "answeredAt")
             VALUES (${a.problemId}, ${a.answer}, ${a.status}::jsonb, ${a.answeredAt})"""
          .as[Insert[Answer]].onConflictIgnore
      }
      ctx.run(q(ctx.lift(toIns)))
    } catch {
      case t: Throwable =>
        println(s"Error while migrating answer ${a}")
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
        u.firstName, u.lastName, u.email,
        LocalDateTime.ofInstant(u.registeredAt.getOrElse(Instant.now()), ZoneOffset.UTC),
        u.lastLogin.map(l => LocalDateTime.ofInstant(l, ZoneOffset.UTC)),
        JsonbValue(u.role))
      /*
            val q = quote {
              query[RegisteredUser].insertValue(ctx.lift(toIns))
            } //?::jsonb
      */

      val req =
        quote { (ru: RegisteredUser) =>
          sql"""INSERT INTO "RegisteredUser" ("id","login","passwordHash","passwordSalt","firstName","lastName","email","registeredAt","lastLogin","role")
               VALUES (${ru.id}, ${ru.login}, ${ru.passwordHash}, ${ru.passwordSalt}, ${ru.firstName}, ${ru.lastName}, ${ru.email},
               ${ru.registeredAt}, ${ru.lastLogin}, ${ru.role}::jsonb)"""
            .as[Insert[RegisteredUser]].onConflictIgnore
        }

      val qr = req

      ctx.run(qr(lift(toIns)))
    }


  }
}
